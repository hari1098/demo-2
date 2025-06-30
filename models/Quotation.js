const mongoose = require('mongoose');

const quotationItemSchema = new mongoose.Schema({
  item: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Item',
    required: true
  },
  quantity: {
    type: Number,
    required: [true, 'Quantity is required'],
    min: [0.01, 'Quantity must be greater than 0']
  },
  unitPrice: {
    type: Number,
    required: [true, 'Unit price is required'],
    min: [0, 'Unit price cannot be negative']
  },
  discount: {
    type: Number,
    default: 0,
    min: [0, 'Discount cannot be negative'],
    max: [100, 'Discount cannot exceed 100%']
  },
  taxRate: {
    type: Number,
    default: 0,
    min: [0, 'Tax rate cannot be negative'],
    max: [100, 'Tax rate cannot exceed 100%']
  }
});

// Virtual for line total
quotationItemSchema.virtual('lineTotal').get(function() {
  const subtotal = this.quantity * this.unitPrice;
  const discountAmount = subtotal * (this.discount / 100);
  const afterDiscount = subtotal - discountAmount;
  const taxAmount = afterDiscount * (this.taxRate / 100);
  return afterDiscount + taxAmount;
});

const quotationSchema = new mongoose.Schema({
  quotationNumber: {
    type: String,
    required: [true, 'Quotation number is required'],
    unique: true,
    trim: true
  },
  customer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Customer',
    required: [true, 'Customer is required']
  },
  items: [quotationItemSchema],
  issueDate: {
    type: Date,
    required: [true, 'Issue date is required'],
    default: Date.now
  },
  validUntil: {
    type: Date,
    required: [true, 'Valid until date is required']
  },
  status: {
    type: String,
    enum: ['draft', 'sent', 'accepted', 'rejected', 'expired'],
    default: 'draft'
  },
  notes: {
    type: String,
    trim: true,
    maxlength: [1000, 'Notes cannot exceed 1000 characters']
  },
  terms: {
    type: String,
    trim: true,
    maxlength: [1000, 'Terms cannot exceed 1000 characters']
  },
  subtotal: {
    type: Number,
    default: 0
  },
  totalDiscount: {
    type: Number,
    default: 0
  },
  totalTax: {
    type: Number,
    default: 0
  },
  total: {
    type: Number,
    default: 0
  },
  createdBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  }
}, {
  timestamps: true
});

// Pre-save middleware to calculate totals
quotationSchema.pre('save', function(next) {
  let subtotal = 0;
  let totalDiscount = 0;
  let totalTax = 0;

  this.items.forEach(item => {
    const itemSubtotal = item.quantity * item.unitPrice;
    const discountAmount = itemSubtotal * (item.discount / 100);
    const afterDiscount = itemSubtotal - discountAmount;
    const taxAmount = afterDiscount * (item.taxRate / 100);

    subtotal += itemSubtotal;
    totalDiscount += discountAmount;
    totalTax += taxAmount;
  });

  this.subtotal = subtotal;
  this.totalDiscount = totalDiscount;
  this.totalTax = totalTax;
  this.total = subtotal - totalDiscount + totalTax;

  next();
});

module.exports = mongoose.model('Quotation', quotationSchema);