import React, { useState, useEffect } from 'react'
import { 
  ShoppingCart, 
  CreditCard, 
  Layers, 
  RefreshCw, 
  AlertCircle, 
  CheckCircle2, 
  Play, 
  ArrowRight,
  Database,
  GitBranch,
  ShieldCheck,
  Cpu,
  RefreshCcw,
  Network
} from 'lucide-react'

function App() {
  const [productId, setProductId] = useState('prod_001')
  const [quantity, setQuantity] = useState(1)
  const [price, setPrice] = useState(15000)
  const [simulateError, setSimulateError] = useState(false)
  const [mode, setMode] = useState('at')
  const [loading, setLoading] = useState(false)
  const [alert, setAlert] = useState(null)
  
  const [orders, setOrders] = useState([])
  const [payments, setPayments] = useState([])
  const [refreshing, setRefreshing] = useState(false)

  const fetchDatabases = async () => {
    try {
      const orderRes = await fetch('http://localhost:8081/api/orders')
      if (orderRes.ok) {
        const json = await orderRes.json()
        if (json.data) setOrders(json.data)
      }
    } catch (e) {
      console.error(e)
    }

    try {
      const paymentRes = await fetch('http://localhost:8082/api/payments')
      if (paymentRes.ok) {
        const json = await paymentRes.json()
        if (json.data) setPayments(json.data)
      }
    } catch (e) {
      console.error(e)
    }
  }

  useEffect(() => {
    fetchDatabases()
    const interval = setInterval(fetchDatabases, 3000)
    return () => clearInterval(interval)
  }, [])

  const handleRefresh = async () => {
    setRefreshing(true)
    await fetchDatabases()
    setRefreshing(false)
  }

  const handleCleanAll = async () => {
    if (!window.confirm('Bạn có chắc chắn muốn xóa sạch toàn bộ dữ liệu trong Database?')) {
      return
    }
    try {
      const response = await fetch('http://localhost:8083/api/coordinator/clean-all', {
        method: 'DELETE'
      })
      const result = await response.json()
      if (result.code === 200) {
        setAlert({
          type: 'success',
          message: 'Đã dọn sạch toàn bộ dữ liệu trong database!'
        })
        setOrders([])
        setPayments([])
      } else {
        setAlert({
          type: 'danger',
          message: `Lỗi khi dọn dữ liệu: ${result.message}`
        })
      }
    } catch (err) {
      setAlert({
        type: 'danger',
        message: `Lỗi kết nối: ${err.message}`
      })
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setAlert(null)

    const url = `http://localhost:8083/api/coordinator/${mode}`
    const payload = {
      productId,
      quantity: parseInt(quantity),
      price: parseFloat(price),
      simulatePaymentError: simulateError
    }

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })
      const result = await response.json()

      if (result.code === 200) {
        setAlert({
          type: 'success',
          message: `${result.message}.`
        })
      } else {
        setAlert({
          type: 'danger',
          message: `Giao dịch thất bại: ${result.message}`
        })
      }
    } catch (err) {
      setAlert({
        type: 'danger',
        message: `Không thể kết nối đến coordinator-service: ${err.message}`
      })
    } finally {
      setLoading(false)
      fetchDatabases()
    }
  }

  const getFlowSteps = () => {
    switch (mode) {
      case 'at':
        return [
          { name: 'Coordinator (TM)', desc: 'Bắt đầu Global Transaction', icon: <Layers size={16} color="#fff" /> },
          { name: 'Order Service (RM)', desc: 'Tạo Order (Lưu dữ liệu tạm thời vào DB)', icon: <ShoppingCart size={16} color="#fff" /> },
          { name: 'Payment Service (RM)', desc: 'Thanh toán (Lưu dữ liệu tạm thời vào DB)', icon: <CreditCard size={16} color="#fff" /> },
          { 
            name: 'Seata Server (TC)', 
            desc: simulateError ? 'Rollback toàn cục dựa trên undo_log' : 'Commit dữ liệu toàn cục thành công', 
            icon: <ShieldCheck size={16} color="#fff" />,
            statusColor: simulateError ? 'var(--danger)' : 'var(--success)'
          }
        ]
      case 'tcc':
        return [
          { name: 'Coordinator (TM)', desc: 'Bắt đầu TCC Transaction', icon: <Layers size={16} color="#fff" /> },
          { name: 'Order Service (Try)', desc: 'Gọi Try (Lưu order PENDING và đăng ký nhánh)', icon: <ShoppingCart size={16} color="#fff" /> },
          { name: 'Payment Service (Try)', desc: 'Gọi Try (Lưu payment PENDING và đăng ký nhánh)', icon: <CreditCard size={16} color="#fff" /> },
          { 
            name: 'Seata Server (2-Phase)', 
            desc: simulateError ? 'Pha 2: Gọi Cancel (Hủy order, giải phóng resource)' : 'Pha 2: Gọi Confirm (Xác nhận Approved)', 
            icon: <RefreshCcw size={16} color="#fff" />,
            statusColor: simulateError ? 'var(--danger)' : 'var(--success)'
          }
        ]
      case 'saga-orch':
        return [
          { name: 'Coordinator (Orchestrator)', desc: 'Điều phối luồng REST API tuần tự', icon: <Cpu size={16} color="#fff" /> },
          { name: 'Order Service', desc: 'Tạo order trạng thái PENDING', icon: <ShoppingCart size={16} color="#fff" /> },
          { name: 'Payment Service', desc: 'Xử lý thanh toán', icon: <CreditCard size={16} color="#fff" /> },
          { 
            name: 'Bù trừ / Xác nhận (Code)', 
            desc: simulateError ? 'Thất bại -> Gọi bù trừ đổi order sang CANCELLED' : 'Thành công -> Đổi order sang APPROVED', 
            icon: <CheckCircle2 size={16} color="#fff" />,
            statusColor: simulateError ? 'var(--danger)' : 'var(--success)'
          }
        ]
      case 'saga-choreo':
        return [
          { name: 'Coordinator Service', desc: 'Gọi Order Service để kích hoạt kịch bản', icon: <Layers size={16} color="#fff" /> },
          { name: 'Order Service', desc: 'Tạo order, phát event ORDER_CREATED vào Kafka', icon: <Network size={16} color="#fff" /> },
          { name: 'Payment Service', desc: 'Lắng nghe Kafka, thanh toán và bắn event kết quả', icon: <CreditCard size={16} color="#fff" /> },
          { 
            name: 'Kafka Event Broker', 
            desc: simulateError ? 'Nhận event thất bại -> Order Service tự tạo order bù trừ âm' : 'Nhận event thành công -> Hoàn tất giao dịch', 
            icon: <GitBranch size={16} color="#fff" />,
            statusColor: simulateError ? 'var(--danger)' : 'var(--success)'
          }
        ]
      default:
        return []
    }
  }

  return (
    <div className="container">
      <header>
        <div className="title-section">
          <h1>Global Transaction Coordinator Dashboard</h1>
          <p>Hệ thống giám sát và điều phối giao dịch phân tán cho Microservices</p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <button 
            onClick={handleCleanAll} 
            className="btn-refresh" 
            style={{ 
              backgroundColor: 'rgba(239, 68, 68, 0.15)', 
              borderColor: 'rgba(239, 68, 68, 0.3)', 
              color: '#f87171',
              padding: '0.5rem 1rem',
              fontSize: '0.85rem',
              fontWeight: '600',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              marginRight: '1rem'
            }}
          >
            <AlertCircle size={14} /> Clean All Data
          </button>
          <span className="badge badge-primary">
            <ShieldCheck size={14} style={{ marginRight: '4px' }} /> Seata Enabled
          </span>
          <span className="badge badge-primary">
            <GitBranch size={14} style={{ marginRight: '4px' }} /> Kafka Enabled
          </span>
          <span className="badge badge-success">Active</span>
        </div>
      </header>

      {alert && (
        <div className={`alert alert-${alert.type}`}>
          {alert.type === 'success' ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}
          <div>{alert.message}</div>
        </div>
      )}

      <div className="grid">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card">
            <h2 className="card-title">
              <Play size={18} color="#6366f1" /> Khởi tạo Giao dịch
            </h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Kịch bản Kiểm thử</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  <div className="toggle-group">
                    <button 
                      type="button" 
                      className={`toggle-btn ${mode === 'at' ? 'active' : ''}`}
                      onClick={() => setMode('at')}
                      style={{ fontSize: '0.8rem' }}
                    >
                      Seata AT
                    </button>
                    <button 
                      type="button" 
                      className={`toggle-btn ${mode === 'tcc' ? 'active' : ''}`}
                      onClick={() => setMode('tcc')}
                      style={{ fontSize: '0.8rem' }}
                    >
                      Seata TCC
                    </button>
                  </div>
                  <div className="toggle-group">
                    <button 
                      type="button" 
                      className={`toggle-btn ${mode === 'saga-orch' ? 'active' : ''}`}
                      onClick={() => setMode('saga-orch')}
                      style={{ fontSize: '0.8rem' }}
                    >
                      Saga Orchestration
                    </button>
                    <button 
                      type="button" 
                      className={`toggle-btn ${mode === 'saga-choreo' ? 'active' : ''}`}
                      onClick={() => setMode('saga-choreo')}
                      style={{ fontSize: '0.8rem' }}
                    >
                      Saga Choreography
                    </button>
                  </div>
                </div>
              </div>

              <div className="form-group">
                <label>Mã Sản Phẩm</label>
                <input 
                  type="text" 
                  className="form-control" 
                  value={productId} 
                  onChange={(e) => setProductId(e.target.value)} 
                  required
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="form-group" style={{ flex: 1 }}>
                  <label>Số lượng</label>
                  <input 
                    type="number" 
                    className="form-control" 
                    value={quantity} 
                    onChange={(e) => setQuantity(e.target.value)} 
                    min="1"
                    required
                  />
                </div>
                <div className="form-group" style={{ flex: 2 }}>
                  <label>Đơn giá (VND)</label>
                  <input 
                    type="number" 
                    className="form-control" 
                    value={price} 
                    onChange={(e) => setPrice(e.target.value)} 
                    min="1"
                    required
                  />
                </div>
              </div>

              <div className="form-group" style={{ margin: '1.5rem 0' }}>
                <label className="checkbox-label">
                  <input 
                    type="checkbox" 
                    checked={simulateError} 
                    onChange={(e) => setSimulateError(e.target.checked)}
                  />
                  <div className="checkbox-custom">
                    {simulateError && <span style={{ width: '10px', height: '10px', backgroundColor: '#fff', borderRadius: '2px' }} />}
                  </div>
                  <span>Giả lập lỗi thanh toán (Rollback/Bù trừ)</span>
                </label>
              </div>

              <button type="submit" className="btn-submit" disabled={loading}>
                {loading ? 'Đang điều phối...' : 'Kích hoạt Giao dịch'} <ArrowRight size={18} />
              </button>
            </form>
          </div>

          <div className="card">
            <h2 className="card-title">
              <GitBranch size={18} color="#10b981" /> Trực quan hóa luồng chạy
            </h2>
            <div className="visual-flow">
              {getFlowSteps().map((step, idx) => (
                <div className="flow-node" key={idx}>
                  <div 
                    className="node-icon active"
                    style={step.statusColor ? { backgroundColor: step.statusColor, borderColor: step.statusColor } : {}}
                  >
                    {step.icon}
                  </div>
                  <div className="node-info">
                    <span className="node-name">{step.name}</span>
                    <span className="node-desc">{step.desc}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="dashboard-sections">
          <div>
            <div className="section-header">
              <h2>
                <Database size={20} color="#6366f1" /> Order Service Database (Cổng 8081)
              </h2>
              <button className="btn-refresh" onClick={handleRefresh} disabled={refreshing}>
                <RefreshCw size={16} className={refreshing ? 'refresh-spin' : ''} />
              </button>
            </div>
            <div className="table-wrapper">
              {orders.length === 0 ? (
                <div className="empty-state">
                  <ShoppingCart size={32} />
                  <p>Chưa có dữ liệu order</p>
                </div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Sản phẩm</th>
                      <th>S.Lượng</th>
                      <th>Giá</th>
                      <th>Trạng thái</th>
                      <th>Transaction ID (Tx ID)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.slice().reverse().map((ord) => (
                      <tr key={ord.id}>
                        <td>{ord.id}</td>
                        <td style={{ fontWeight: 600 }}>{ord.productId}</td>
                        <td>{ord.quantity}</td>
                        <td>{ord.price?.toLocaleString()} đ</td>
                        <td>
                          <span className={`badge ${
                            ord.status === 'APPROVED' ? 'badge-success' : 
                            ord.status === 'PENDING' ? 'badge-warning' : 
                            'badge-danger'
                          }`}>
                            {ord.status}
                          </span>
                        </td>
                        <td style={{ fontSize: '0.72rem', fontFamily: 'monospace', color: 'var(--text-secondary)' }}>
                          {ord.txId || 'N/A'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          <div>
            <div className="section-header">
              <h2>
                <Database size={20} color="#10b981" /> Payment Service Database (Cổng 8082)
              </h2>
            </div>
            <div className="table-wrapper">
              {payments.length === 0 ? (
                <div className="empty-state">
                  <CreditCard size={32} />
                  <p>Chưa có dữ liệu payment</p>
                </div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Mã Order</th>
                      <th>Số tiền</th>
                      <th>Trạng thái</th>
                      <th>Transaction ID (Tx ID)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {payments.slice().reverse().map((pm) => (
                      <tr key={pm.id}>
                        <td>{pm.id}</td>
                        <td>{pm.orderId}</td>
                        <td style={{ fontWeight: 600 }}>{pm.amount?.toLocaleString()} đ</td>
                        <td>
                          <span className={`badge ${
                            pm.status === 'SUCCESS' ? 'badge-success' : 
                            pm.status === 'COMPENSATED' ? 'badge-warning' : 
                            'badge-danger'
                          }`}>
                            {pm.status}
                          </span>
                        </td>
                        <td style={{ fontSize: '0.72rem', fontFamily: 'monospace', color: 'var(--text-secondary)' }}>
                          {pm.txId || 'N/A'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
