const express = require('express');
const Invoice = require('../models/Invoice');
const Item = require('../models/Item');
const Customer = require('../models/Customer');
const Quotation = require('../models/Quotation');
const { authenticateToken } = require('../middleware/auth');
const { validateInvoice } = require('../middleware/validation');

const router = express.Router();

// Apply authentication to all routes
router.use(authenticateToken);

// Get all invoices with pagination and search
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const search = req.query.search || '';
    const status = req.query.status || '';
    const paymentStatus = req.query.paymentStatus || '';
    const skip = (page - 1) * limit;

    let query = {};
    
    if (search) {
      query.$or = [
        { invoiceNumber: { $regex: search, $options: 'i' } }
      ];
    }

    if (status) {
      query.status = status;
    }

    if (paymentStatus) {
      query.paymentStatus = paymentStatus;
    }

    const invoices = await Invoice.find(query)
      .populate('customer', 'name email')
      .populate('quotation', 'quotationNumber')
      .populate('items.item', 'name sku')
      .populate('createdBy', 'username')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const total = await Invoice.countDocuments(query);

    res.json({
      invoices,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Get invoices error:', error);
    res.status(500).json({ error: 'Failed to fetch invoices' });
  }
});

// Get invoice by ID
router.get('/:id', async (req, res) => {
  try {
    const invoice = await Invoice.findById(req.params.id)
      .populate('customer')
      .populate('quotation')
      .populate('items.item')
      .populate('createdBy', 'username');

    if (!invoice) {
      return res.status(404).json({ error: 'Invoice not found' });
    }

    res.json(invoice);
  } catch (error) {
    console.error('Get invoice error:', error);
    res.status(500).json({ error: 'Failed to fetch invoice' });
  }
});

// Create new invoice
router.post('/', validateInvoice, async (req, res) => {
  try {
    const { 
      invoiceNumber, 
      customer, 
      quotation, 
      items, 
      dueDate, 
      paymentMethod,
      notes, 
      terms 
    } = req.body;

    // Check if invoice number already exists
    const existingInvoice = await Invoice.findOne({ invoiceNumber });
    if (existingInvoice) {
      return res.status(400).json({
        error: 'Invoice number already exists'
      });
    }

    // Verify customer exists
    const customerExists = await Customer.findOne({
      _id: customer,
      isActive: true
    });
    if (!customerExists) {
      return res.status(400).json({ error: 'Customer not found' });
    }

    // Verify quotation exists if provided
    if (quotation) {
      const quotationExists = await Quotation.findById(quotation);
      if (!quotationExists) {
        return res.status(400).json({ error: 'Quotation not found' });
      }
    }

    // Verify all items exist and are active
    const itemIds = items.map(item => item.item);
    const existingItems = await Item.find({
      _id: { $in: itemIds },
      isActive: true
    });

    if (existingItems.length !== itemIds.length) {
      return res.status(400).json({
        error: 'One or more items not found or inactive'
      });
    }

    // Update stock quantities
    for (const invoiceItem of items) {
      const item = existingItems.find(i => i._id.toString() === invoiceItem.item);
      if (item.stockQuantity < invoiceItem.quantity) {
        return res.status(400).json({
          error: `Insufficient stock for item: ${item.name}. Available: ${item.stockQuantity}, Required: ${invoiceItem.quantity}`
        });
      }
      
      await Item.findByIdAndUpdate(
        invoiceItem.item,
        { $inc: { stockQuantity: -invoiceItem.quantity } }
      );
    }

    const invoiceData = {
      invoiceNumber,
      customer,
      quotation,
      items,
      dueDate,
      paymentMethod,
      notes,
      terms,
      createdBy: req.user._id
    };

    const invoice = new Invoice(invoiceData);
    await invoice.save();
    
    await invoice.populate([
      { path: 'customer' },
      { path: 'quotation' },
      { path: 'items.item' },
      { path: 'createdBy', select: 'username' }
    ]);

    res.status(201).json({
      message: 'Invoice created successfully',
      invoice
    });
  } catch (error) {
    console.error('Create invoice error:', error);
    res.status(500).json({ error: 'Failed to create invoice' });
  }
});

// Update invoice
router.put('/:id', validateInvoice, async (req, res) => {
  try {
    const invoiceId = req.params.id;
    const { 
      invoiceNumber, 
      customer, 
      quotation, 
      items, 
      dueDate, 
      paymentMethod,
      notes, 
      terms,
      status 
    } = req.body;

    // Check if invoice exists
    const existingInvoice = await Invoice.findById(invoiceId);
    if (!existingInvoice) {
      return res.status(404).json({ error: 'Invoice not found' });
    }

    // Don't allow editing paid invoices
    if (existingInvoice.paymentStatus === 'paid') {
      return res.status(400).json({
        error: 'Cannot edit a paid invoice'
      });
    }

    // Check if invoice number already exists (excluding current invoice)
    if (invoiceNumber && invoiceNumber !== existingInvoice.invoiceNumber) {
      const duplicateInvoice = await Invoice.findOne({
        invoiceNumber,
        _id: { $ne: invoiceId }
      });
      if (duplicateInvoice) {
        return res.status(400).json({
          error: 'Invoice number already exists'
        });
      }
    }

    // Verify customer exists
    if (customer) {
      const customerExists = await Customer.findOne({
        _id: customer,
        isActive: true
      });
      if (!customerExists) {
        return res.status(400).json({ error: 'Customer not found' });
      }
    }

    // Verify quotation exists if provided
    if (quotation) {
      const quotationExists = await Quotation.findById(quotation);
      if (!quotationExists) {
        return res.status(400).json({ error: 'Quotation not found' });
      }
    }

    // Handle stock updates if items changed
    if (items) {
      // Restore stock for old items
      for (const oldItem of existingInvoice.items) {
        await Item.findByIdAndUpdate(
          oldItem.item,
          { $inc: { stockQuantity: oldItem.quantity } }
        );
      }

      // Verify and deduct stock for new items
      const itemIds = items.map(item => item.item);
      const existingItems = await Item.find({
        _id: { $in: itemIds },
        isActive: true
      });

      if (existingItems.length !== itemIds.length) {
        return res.status(400).json({
          error: 'One or more items not found or inactive'
        });
      }

      for (const invoiceItem of items) {
        const item = existingItems.find(i => i._id.toString() === invoiceItem.item);
        if (item.stockQuantity < invoiceItem.quantity) {
          return res.status(400).json({
            error: `Insufficient stock for item: ${item.name}. Available: ${item.stockQuantity}, Required: ${invoiceItem.quantity}`
          });
        }
        
        await Item.findByIdAndUpdate(
          invoiceItem.item,
          { $inc: { stockQuantity: -invoiceItem.quantity } }
        );
      }
    }

    const updateData = {
      invoiceNumber,
      customer,
      quotation,
      items,
      dueDate,
      paymentMethod,
      notes,
      terms,
      status
    };

    // Remove undefined fields
    Object.keys(updateData).forEach(key => 
      updateData[key] === undefined && delete updateData[key]
    );

    const invoice = await Invoice.findByIdAndUpdate(
      invoiceId,
      updateData,
      { new: true, runValidators: true }
    ).populate([
      { path: 'customer' },
      { path: 'quotation' },
      { path: 'items.item' },
      { path: 'createdBy', select: 'username' }
    ]);

    res.json({
      message: 'Invoice updated successfully',
      invoice
    });
  } catch (error) {
    console.error('Update invoice error:', error);
    res.status(500).json({ error: 'Failed to update invoice' });
  }
});

// Record payment
router.post('/:id/payments', async (req, res) => {
  try {
    const { amount, paymentMethod, notes } = req.body;

    if (!amount || amount <= 0) {
      return res.status(400).json({
        error: 'Payment amount must be greater than 0'
      });
    }

    const invoice = await Invoice.findById(req.params.id);
    if (!invoice) {
      return res.status(404).json({ error: 'Invoice not found' });
    }

    const newAmountPaid = invoice.amountPaid + amount;
    if (newAmountPaid > invoice.total) {
      return res.status(400).json({
        error: 'Payment amount exceeds invoice total'
      });
    }

    invoice.amountPaid = newAmountPaid;
    if (paymentMethod) {
      invoice.paymentMethod = paymentMethod;
    }
    
    await invoice.save();

    await invoice.populate([
      { path: 'customer' },
      { path: 'quotation' },
      { path: 'items.item' },
      { path: 'createdBy', select: 'username' }
    ]);

    res.json({
      message: 'Payment recorded successfully',
      invoice,
      paymentDetails: {
        amount,
        paymentMethod,
        notes,
        balanceDue: invoice.total - invoice.amountPaid
      }
    });
  } catch (error) {
    console.error('Record payment error:', error);
    res.status(500).json({ error: 'Failed to record payment' });
  }
});

// Update invoice status
router.patch('/:id/status', async (req, res) => {
  try {
    const { status } = req.body;
    const validStatuses = ['draft', 'sent', 'paid', 'overdue', 'cancelled'];

    if (!validStatuses.includes(status)) {
      return res.status(400).json({
        error: 'Invalid status. Must be one of: ' + validStatuses.join(', ')
      });
    }

    const invoice = await Invoice.findByIdAndUpdate(
      req.params.id,
      { status },
      { new: true, runValidators: true }
    ).populate([
      { path: 'customer' },
      { path: 'quotation' },
      { path: 'items.item' },
      { path: 'createdBy', select: 'username' }
    ]);

    if (!invoice) {
      return res.status(404).json({ error: 'Invoice not found' });
    }

    res.json({
      message: 'Invoice status updated successfully',
      invoice
    });
  } catch (error) {
    console.error('Update invoice status error:', error);
    res.status(500).json({ error: 'Failed to update invoice status' });
  }
});

// Delete invoice
router.delete('/:id', async (req, res) => {
  try {
    const invoice = await Invoice.findById(req.params.id);
    if (!invoice) {
      return res.status(404).json({ error: 'Invoice not found' });
    }

    // Don't allow deleting paid invoices
    if (invoice.paymentStatus === 'paid') {
      return res.status(400).json({
        error: 'Cannot delete a paid invoice'
      });
    }

    // Restore stock quantities
    for (const item of invoice.items) {
      await Item.findByIdAndUpdate(
        item.item,
        { $inc: { stockQuantity: item.quantity } }
      );
    }

    await Invoice.findByIdAndDelete(req.params.id);

    res.json({ message: 'Invoice deleted successfully' });
  } catch (error) {
    console.error('Delete invoice error:', error);
    res.status(500).json({ error: 'Failed to delete invoice' });
  }
});

// Generate invoice number
router.get('/meta/next-number', async (req, res) => {
  try {
    const currentYear = new Date().getFullYear();
    const prefix = `INV-${currentYear}-`;
    
    const lastInvoice = await Invoice.findOne({
      invoiceNumber: { $regex: `^${prefix}` }
    }).sort({ invoiceNumber: -1 });

    let nextNumber = 1;
    if (lastInvoice) {
      const lastNumber = parseInt(lastInvoice.invoiceNumber.split('-').pop());
      nextNumber = lastNumber + 1;
    }

    const invoiceNumber = `${prefix}${nextNumber.toString().padStart(4, '0')}`;

    res.json({ invoiceNumber });
  } catch (error) {
    console.error('Generate invoice number error:', error);
    res.status(500).json({ error: 'Failed to generate invoice number' });
  }
});

// Get overdue invoices
router.get('/reports/overdue', async (req, res) => {
  try {
    const today = new Date();
    const overdueInvoices = await Invoice.find({
      dueDate: { $lt: today },
      paymentStatus: { $ne: 'paid' },
      status: { $ne: 'cancelled' }
    })
    .populate('customer', 'name email phone')
    .populate('createdBy', 'username')
    .sort({ dueDate: 1 });

    res.json({ overdueInvoices });
  } catch (error) {
    console.error('Get overdue invoices error:', error);
    res.status(500).json({ error: 'Failed to fetch overdue invoices' });
  }
});

module.exports = router;