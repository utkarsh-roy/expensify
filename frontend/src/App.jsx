import { useEffect, useState } from 'react'
import './App.css'
import {
  createExpense,
  deleteExpense,
  getExpenseAnalysis,
  getExpenses,
  updateExpense,
} from './services/expenseApi'

const initialForm = {
  title: '',
  category: '',
  amount: '',
  expenseDate: '',
  notes: '',
}

function App() {
  const [expenses, setExpenses] = useState([])
  const [formData, setFormData] = useState(initialForm)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [analysis, setAnalysis] = useState(null)
  const [analysisLoading, setAnalysisLoading] = useState(false)

  useEffect(() => {
    loadExpenses()
  }, [])

  async function loadExpenses() {
    setLoading(true)
    setError('')

    try {
      const data = await getExpenses()
      setExpenses(data)
    } catch (loadError) {
      setError(loadError.message)
    } finally {
      setLoading(false)
    }
  }

  function handleChange(event) {
    const { name, value } = event.target
    setFormData((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError('')

    try {
      if (editingId) {
        await updateExpense(editingId, formData)
      } else {
        await createExpense(formData)
      }

      setFormData(initialForm)
      setEditingId(null)
      await loadExpenses()
    } catch (submitError) {
      setError(submitError.message)
    } finally {
      setSubmitting(false)
    }
  }

  function handleEdit(expense) {
    setEditingId(expense.id)
    setFormData({
      title: expense.title,
      category: expense.category,
      amount: expense.amount,
      expenseDate: expense.expenseDate,
      notes: expense.notes ?? '',
    })
  }

  function handleCancelEdit() {
    setEditingId(null)
    setFormData(initialForm)
  }

  async function handleDelete(id) {
    setError('')

    try {
      await deleteExpense(id)
      if (editingId === id) {
        handleCancelEdit()
      }
      await loadExpenses()
    } catch (deleteError) {
      setError(deleteError.message)
    }
  }

  async function handleGenerateAnalysis() {
    setAnalysisLoading(true)
    setError('')

    try {
      const response = await getExpenseAnalysis()
      setAnalysis(response)
    } catch (analysisError) {
      setError(analysisError.message)
    } finally {
      setAnalysisLoading(false)
    }
  }

  const totalAmount = expenses.reduce(
    (sum, expense) => sum + Number(expense.amount),
    0,
  )

  return (
    <main className="app-shell">
      <section className="hero-panel">
        <div>
          <p className="eyebrow">Expense Tracker</p>
          <h1>Track spending with a clean Spring Boot and React setup.</h1>
          <p className="hero-copy">
            Create, update, and delete personal expenses with a lightweight UI
            and a simple REST API.
          </p>
        </div>
        <div className="summary-card">
          <span>Total expenses</span>
          <strong>₹ {totalAmount.toFixed(2)}</strong>
          <p>{expenses.length} recorded item(s)</p>
        </div>
      </section>

      {error ? <p className="status error">{error}</p> : null}

      <section className="content-grid">
        <article className="panel form-panel">
          <div className="panel-header">
            <div>
              <p className="panel-label">Manage Expense</p>
              <h2>{editingId ? 'Update expense' : 'Add expense'}</h2>
            </div>
            {editingId ? (
              <button
                type="button"
                className="secondary-button"
                onClick={handleCancelEdit}
              >
                Cancel
              </button>
            ) : null}
          </div>

          <form className="expense-form" onSubmit={handleSubmit}>
            <label>
              Title
              <input
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="Groceries"
                required
              />
            </label>

            <label>
              Category
              <input
                name="category"
                value={formData.category}
                onChange={handleChange}
                placeholder="Food"
                required
              />
            </label>

            <div className="form-row">
              <label>
                Amount
                <input
                  name="amount"
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={formData.amount}
                  onChange={handleChange}
                  placeholder="120.50"
                  required
                />
              </label>

              <label>
                Date
                <input
                  name="expenseDate"
                  type="date"
                  value={formData.expenseDate}
                  onChange={handleChange}
                  required
                />
              </label>
            </div>

            <label>
              Notes
              <textarea
                name="notes"
                value={formData.notes}
                onChange={handleChange}
                rows="4"
                placeholder="Optional details"
              />
            </label>

            <button type="submit" className="primary-button" disabled={submitting}>
              {submitting
                ? 'Saving...'
                : editingId
                  ? 'Update Expense'
                  : 'Add Expense'}
            </button>
          </form>
        </article>

        <article className="panel list-panel">
          <div className="panel-header">
            <div>
              <p className="panel-label">Expense List</p>
              <h2>Recent entries</h2>
            </div>
            <button
              type="button"
              className="secondary-button"
              onClick={loadExpenses}
              disabled={loading}
            >
              Refresh
            </button>
          </div>

          {loading ? <p className="status">Loading expenses...</p> : null}

          {!loading && expenses.length === 0 ? (
            <p className="status">No expenses yet. Add your first one.</p>
          ) : null}

          <div className="expense-list">
            {expenses.map((expense) => (
              <article key={expense.id} className="expense-card">
                <div className="expense-card-top">
                  <div>
                    <h3>{expense.title}</h3>
                    <p className="expense-meta">
                      {expense.category} • {expense.expenseDate}
                    </p>
                  </div>
                  <strong>₹ {Number(expense.amount).toFixed(2)}</strong>
                </div>

                {expense.notes ? <p className="expense-notes">{expense.notes}</p> : null}

                <div className="expense-actions">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => handleEdit(expense)}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    className="danger-button"
                    onClick={() => handleDelete(expense.id)}
                  >
                    Delete
                  </button>
                </div>
              </article>
            ))}
          </div>
        </article>
      </section>

      <section className="panel analysis-panel">
        <div className="panel-header">
          <div>
            <p className="panel-label">AI Analysis</p>
            <h2>OpenAI spending insights</h2>
          </div>
          <button
            type="button"
            className="primary-button"
            onClick={handleGenerateAnalysis}
            disabled={analysisLoading || expenses.length === 0}
          >
            {analysisLoading ? 'Analyzing...' : 'Generate Insights'}
          </button>
        </div>

        {expenses.length === 0 ? (
          <p className="status">Add expenses first to generate analysis.</p>
        ) : null}

        {analysis ? (
          <div className="analysis-grid">
            <div className="analysis-card">
              <span className="analysis-label">Model</span>
              <strong>{analysis.model}</strong>
            </div>
            <div className="analysis-card">
              <span className="analysis-label">Entries</span>
              <strong>{analysis.totalEntries}</strong>
            </div>
            <div className="analysis-card">
              <span className="analysis-label">Tracked spend</span>
              <strong>₹ {Number(analysis.totalExpense).toFixed(2)}</strong>
            </div>
            <div className="analysis-card analysis-text">
              <span className="analysis-label">AI summary</span>
              <p>{analysis.aiInsights}</p>
            </div>
            <div className="analysis-card analysis-text">
              <span className="analysis-label">Category breakdown</span>
              <ul className="breakdown-list">
                {Object.entries(analysis.categoryBreakdown).map(([category, amount]) => (
                  <li key={category}>
                    <span>{category}</span>
                    <strong>₹ {Number(amount).toFixed(2)}</strong>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        ) : (
          expenses.length > 0 && (
            <p className="status">
              Generate analysis to get an AI summary and spending suggestions.
            </p>
          )
        )}
      </section>
    </main>
  )
}

export default App
