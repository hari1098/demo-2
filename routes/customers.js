const express = require('express');
const Customer = require('../models/Customer');
const { authenticateToken } = require('../middleware/auth');
const { validateCustomer } = require('../middleware/validation');

const router = express.Router();

// Apply authentication to all routes
router.use(authenticateToken);

// Get all customers with pagination and search
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const search = req.query.search || '';
    const skip = (page - 1) * limit;

    let query = { isActive: true };
    
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { email: { $regex: search, $options: 'i' } },
        { phone: { $regex: search, $options: 'i' } }
      ];
    }

    const customers = await Customer.find(query)
      .populate('createdBy', 'username')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const total = await Customer.countDocuments(query);

    res.json({
      customers,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Get customers error:', error);
    res.status(500).json({ error: 'Failed to fetch customers' });
  }
});

// Get customer by ID
router.get('/:id', async (req, res) => {
  try {
    const customer = await Customer.findOne({
      _id: req.params.id,
      isActive: true
    }).populate('createdBy', 'username');

    if (!customer) {
      return res.status(404).json({ error: 'Customer not found' });
    }

    res.json(customer);
  } catch (error) {
    console.error('Get customer error:', error);
    res.status(500).json({ error: 'Failed to fetch customer' });
  }
});

// Create new customer
router.post('/', validateCustomer, async (req, res) => {
  try {
    const customerData = {
      ...req.body,
      createdBy: req.user._id
    };

    // Check if customer with same email already exists
    const existingCustomer = await Customer.findOne({
      email: req.body.email,
      isActive: true
    });

    if (existingCustomer) {
      return res.status(400).json({
        error: 'Customer with this email already exists'
      });
    }

    const customer = new Customer(customerData);
    await customer.save();
    
    await customer.populate('createdBy', 'username');

    res.status(201).json({
      message: 'Customer created successfully',
      customer
    });
  } catch (error) {
    console.error('Create customer error:', error);
    if (error.code === 11000) {
      res.status(400).json({ error: 'Customer with this email already exists' });
    } else {
      res.status(500).json({ error: 'Failed to create customer' });
    }
  }
});

// Update customer
router.put('/:id', validateCustomer, async (req, res) => {
  try {
    const customerId = req.params.id;

    // Check if customer with same email already exists (excluding current customer)
    if (req.body.email) {
      const existingCustomer = await Customer.findOne({
        email: req.body.email,
        _id: { $ne: customerId },
        isActive: true
      });

      if (existingCustomer) {
        return res.status(400).json({
          error: 'Another customer with this email already exists'
        });
      }
    }

    const customer = await Customer.findOneAndUpdate(
      { _id: customerId, isActive: true },
      req.body,
      { new: true, runValidators: true }
    ).populate('createdBy', 'username');

    if (!customer) {
      return res.status(404).json({ error: 'Customer not found' });
    }

    res.json({
      message: 'Customer updated successfully',
      customer
    });
  } catch (error) {
    console.error('Update customer error:', error);
    res.status(500).json({ error: 'Failed to update customer' });
  }
});

// Soft delete customer
router.delete('/:id', async (req, res) => {
  try {
    const customer = await Customer.findOneAndUpdate(
      { _id: req.params.id, isActive: true },
      { isActive: false },
      { new: true }
    );

    if (!customer) {
      return res.status(404).json({ error: 'Customer not found' });
    }

    res.json({ message: 'Customer deleted successfully' });
  } catch (error) {
    console.error('Delete customer error:', error);
    res.status(500).json({ error: 'Failed to delete customer' });
  }
});

module.exports = router;