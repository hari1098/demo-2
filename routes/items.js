const express = require('express');
const Item = require('../models/Item');
const { authenticateToken } = require('../middleware/auth');
const { validateItem } = require('../middleware/validation');

const router = express.Router();

// Apply authentication to all routes
router.use(authenticateToken);

// Get all items with pagination and search
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const search = req.query.search || '';
    const category = req.query.category || '';
    const skip = (page - 1) * limit;

    let query = { isActive: true };
    
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { sku: { $regex: search, $options: 'i' } },
        { description: { $regex: search, $options: 'i' } }
      ];
    }

    if (category) {
      query.category = { $regex: category, $options: 'i' };
    }

    const items = await Item.find(query)
      .populate('createdBy', 'username')
      .sort({ createdAt: -1 })
      .skip(skip)
      .limit(limit);

    const total = await Item.countDocuments(query);

    res.json({
      items,
      pagination: {
        page,
        limit,
        total,
        pages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    console.error('Get items error:', error);
    res.status(500).json({ error: 'Failed to fetch items' });
  }
});

// Get item by ID
router.get('/:id', async (req, res) => {
  try {
    const item = await Item.findOne({
      _id: req.params.id,
      isActive: true
    }).populate('createdBy', 'username');

    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    res.json(item);
  } catch (error) {
    console.error('Get item error:', error);
    res.status(500).json({ error: 'Failed to fetch item' });
  }
});

// Get low stock items
router.get('/alerts/low-stock', async (req, res) => {
  try {
    const items = await Item.find({
      isActive: true,
      $expr: { $lte: ['$stockQuantity', '$minStockLevel'] }
    }).populate('createdBy', 'username');

    res.json({ items });
  } catch (error) {
    console.error('Get low stock items error:', error);
    res.status(500).json({ error: 'Failed to fetch low stock items' });
  }
});

// Get categories
router.get('/meta/categories', async (req, res) => {
  try {
    const categories = await Item.distinct('category', { isActive: true });
    res.json({ categories });
  } catch (error) {
    console.error('Get categories error:', error);
    res.status(500).json({ error: 'Failed to fetch categories' });
  }
});

// Create new item
router.post('/', validateItem, async (req, res) => {
  try {
    const itemData = {
      ...req.body,
      createdBy: req.user._id
    };

    // Check if item with same SKU already exists
    const existingItem = await Item.findOne({
      sku: req.body.sku.toUpperCase(),
      isActive: true
    });

    if (existingItem) {
      return res.status(400).json({
        error: 'Item with this SKU already exists'
      });
    }

    const item = new Item(itemData);
    await item.save();
    
    await item.populate('createdBy', 'username');

    res.status(201).json({
      message: 'Item created successfully',
      item
    });
  } catch (error) {
    console.error('Create item error:', error);
    if (error.code === 11000) {
      res.status(400).json({ error: 'Item with this SKU already exists' });
    } else {
      res.status(500).json({ error: 'Failed to create item' });
    }
  }
});

// Update item
router.put('/:id', validateItem, async (req, res) => {
  try {
    const itemId = req.params.id;

    // Check if item with same SKU already exists (excluding current item)
    if (req.body.sku) {
      const existingItem = await Item.findOne({
        sku: req.body.sku.toUpperCase(),
        _id: { $ne: itemId },
        isActive: true
      });

      if (existingItem) {
        return res.status(400).json({
          error: 'Another item with this SKU already exists'
        });
      }
    }

    const item = await Item.findOneAndUpdate(
      { _id: itemId, isActive: true },
      req.body,
      { new: true, runValidators: true }
    ).populate('createdBy', 'username');

    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    res.json({
      message: 'Item updated successfully',
      item
    });
  } catch (error) {
    console.error('Update item error:', error);
    res.status(500).json({ error: 'Failed to update item' });
  }
});

// Update stock quantity
router.patch('/:id/stock', async (req, res) => {
  try {
    const { stockQuantity } = req.body;

    if (typeof stockQuantity !== 'number' || stockQuantity < 0) {
      return res.status(400).json({
        error: 'Stock quantity must be a non-negative number'
      });
    }

    const item = await Item.findOneAndUpdate(
      { _id: req.params.id, isActive: true },
      { stockQuantity },
      { new: true, runValidators: true }
    ).populate('createdBy', 'username');

    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    res.json({
      message: 'Stock quantity updated successfully',
      item
    });
  } catch (error) {
    console.error('Update stock error:', error);
    res.status(500).json({ error: 'Failed to update stock quantity' });
  }
});

// Soft delete item
router.delete('/:id', async (req, res) => {
  try {
    const item = await Item.findOneAndUpdate(
      { _id: req.params.id, isActive: true },
      { isActive: false },
      { new: true }
    );

    if (!item) {
      return res.status(404).json({ error: 'Item not found' });
    }

    res.json({ message: 'Item deleted successfully' });
  } catch (error) {
    console.error('Delete item error:', error);
    res.status(500).json({ error: 'Failed to delete item' });
  }
});

module.exports = router;