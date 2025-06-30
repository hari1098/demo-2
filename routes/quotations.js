const express = require('express');
const Quotation = require('../models/Quotation');
const Item = require('../models/Item');
const Customer = require('../models/Customer');
const { authenticateToken } = require('../middleware/auth');
const { validateQuotation } = require('../middleware/validation');

const router = express.Router();

// Apply authentication to all routes
router.use(authenticateToken);

// Get all quotations with pagination and search
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const search = req.query.search || '';
    const status = req.query.status || '';
    const skip = (page - 1) * limit;

    let query = {};
    
    if (search) {
      query.$or = [
        { quotationNumber: { $regex: search, $options: 'i' } }
      ];
    }

    if (status) {
      query.status = status;
    }

    const quotations = await Quotation.find(query)
      .populate('customer', 'name email')
      .populate('items.item', 'name sku')
      .populate('createdBy', 'username')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const total = await Quotation.countDocuments(query);

    res.json({
      quotations,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Get quotations error:', error);
    res.status(500).json({ error: 'Failed to fetch quotations' });
  }
});

// Get quotation by ID
router.get('/:id', async (req, res) => {
  try {
    const quotation = await Quotation.findById(req.params.id)
      .populate('customer')
      .populate('items.item')
      .populate('createdBy', 'username');

    if (!quotation) {
      return res.status(404).json({ error: 'Quotation not found' });
    }

    res.json(quotation);
  } catch (error) {
    console.error('Get quotation error:', error);
    res.status(500).json({ error: 'Failed to fetch quotation' });
  }
});

// Create new quotation
router.post('/', validateQuotation, async (req, res) => {
  try {
    const { quotationNumber, customer, items, validUntil, notes, terms } = req.body;

    // Check if quotation number already exists
    const existingQuotation = await Quotation.findOne({ quotationNumber });
    if (existingQuotation) {
      return res.status(400).json({
        error: 'Quotation number already exists'
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

    const quotationData = {
      quotationNumber,
      customer,
      items,
      validUntil,
      notes,
      terms,
      createdBy: req.user._id
    };

    const quotation = new Quotation(quotationData);
    await quotation.save();
    
    await quotation.populate([
      { path: 'customer' },
      { path: 'items.item' },
      { path: 'createdBy', select: 'username' }
    ]);

    res.status(201).json({
      message: 'Quotation created successfully',
      quotation
    });
  } catch (error) {
    console.error('Create quotation error:', error);
    res.status(500).json({ error: 'Failed to create quotation' });
  }
});

// Update quotation
router.put('/:id', validateQuotation, async (req, res) => {
  try {
    const quotationId = req.params.id;
    const { quotationNumber, customer, items, validUntil, notes, terms, status } = req.body;

    // Check if quotation exists
    const existingQuotation = await Quotation.findById(quotationId);
    if (!existingQuotation) {
      return res.status(404).json({ error: 'Quotation not found' });
    }

    // Check if quotation number already exists (excluding current quotation)
    if (quotationNumber && quotationNumber !== existingQuotation.quotationNumber) {
      const duplicateQuotation = await Quotation.findOne({
        quotationNumber,
        _id: { $ne: quotationId }
      });
      if (duplicateQuotation) {
        return res.status(400).json({
          error: 'Quotation number already exists'
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

    // Verify all items exist and are active
    if (items) {
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
    }

    const updateData = {
      quotationNumber,
      customer,
      items,
      validUntil,
      notes,
      terms,
      status
    };

    // Remove undefined fields
    Object.keys(updateData).forEach(key => 
      updateData[key] === undefined && delete updateData[key]
    );

    const quotation = await Quotation.findByIdAndUpdate(
      quotationId,
      updateData,
      { new: true, runValidators: true }
    ).populate([
      { path: 'customer' },
      { path: 'items.item' },
      { path: 'createdBy', select: 'username' }
    ]);

    res.json({
      message: 'Quotation updated successfully',
      quotation
    });
  } catch (error) {
    console.error('Update quotation error:', error);
    res.status(500).json({ error: 'Failed to update quotation' });
  }
});

// Update quotation status
router.patch('/:id/status', async (req, res) => {
  try {
    const { status } = req.body;
    const validStatuses = ['draft', 'sent', 'accepted', 'rejected', 'expired'];

    if (!validStatuses.includes(status)) {
      return res.status(400).json({
        error: 'Invalid status. Must be one of: ' + validStatuses.join(', ')
      });
    }

    const quotation = await Quotation.findByIdAndUpdate(
      req.params.id,
      { status },
      { new: true, runValidators: true }
    ).populate([
      { path: 'customer' },
      { path: 'items.item' },
      { path: 'createdBy', select: 'username' }
    ]);

    if (!quotation) {
      return res.status(404).json({ error: 'Quotation not found' });
    }

    res.json({
      message: 'Quotation status updated successfully',
      quotation
    });
  } catch (error) {
    console.error('Update quotation status error:', error);
    res.status(500).json({ error: 'Failed to update quotation status' });
  }
});

// Delete quotation
router.delete('/:id', async (req, res) => {
  try {
    const quotation = await Quotation.findByIdAndDelete(req.params.id);

    if (!quotation) {
      return res.status(404).json({ error: 'Quotation not found' });
    }

    res.json({ message: 'Quotation deleted successfully' });
  } catch (error) {
    console.error('Delete quotation error:', error);
    res.status(500).json({ error: 'Failed to delete quotation' });
  }
});

// Generate quotation number
router.get('/meta/next-number', async (req, res) => {
  try {
    const currentYear = new Date().getFullYear();
    const prefix = `QUO-${currentYear}-`;
    
    const lastQuotation = await Quotation.findOne({
      quotationNumber: { $regex: `^${prefix}` }
    }).sort({ quotationNumber: -1 });

    let nextNumber = 1;
    if (lastQuotation) {
      const lastNumber = parseInt(lastQuotation.quotationNumber.split('-').pop());
      nextNumber = lastNumber + 1;
    }

    const quotationNumber = `${prefix}${nextNumber.toString().padStart(4, '0')}`;

    res.json({ quotationNumber });
  } catch (error) {
    console.error('Generate quotation number error:', error);
    res.status(500).json({ error: 'Failed to generate quotation number' });
  }
});

module.exports = router;