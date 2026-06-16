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
  Network,
  Terminal,
  Trash2
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

  const [consoleLogs, setConsoleLogs] = useState([
    { time: new Date().toTimeString().split(' ')[0], text: 'Hệ thống giám sát đã khởi động. Sẵn sàng kiểm thử giao dịch.', type: 'info' }
  ])
  const [stepStates, setStepStates] = useState([])
  const terminalEndRef = React.useRef(null)

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

  useEffect(() => {
    if (terminalEndRef.current) {
      terminalEndRef.current.scrollIntoView({ behavior: 'smooth' })
    }
  }, [consoleLogs])

  useEffect(() => {
    const stepsLength = getFlowSteps().length
    setStepStates(Array(stepsLength).fill('pending'))
  }, [mode])

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
        setConsoleLogs(prev => [...prev, {
          time: new Date().toTimeString().split(' ')[0],
          text: '[System] Đã dọn sạch toàn bộ dữ liệu trong database!',
          type: 'warning'
        }])
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

  const getFlowSteps = () => {
    switch (mode) {
      case 'at':
        return [
          { name: 'Coordinator (TM)', desc: 'Bắt đầu Global Transaction (REST)', icon: <Layers size={14} color="#fff" /> },
          { name: 'Order Service (RM)', desc: 'REST POST /api/orders/pure-at (Lưu tạm)', icon: <ShoppingCart size={14} color="#fff" /> },
          { name: 'Payment Service (RM)', desc: 'REST POST /api/payments/at (Lưu tạm)', icon: <CreditCard size={14} color="#fff" /> },
          { name: 'Seata Server (TC)', desc: 'Commit / Rollback toàn cục qua undo_log', icon: <ShieldCheck size={14} color="#fff" /> }
        ]
      case 'grpc-at':
        return [
          { name: 'Coordinator (TM)', desc: 'Bắt đầu Global Transaction (gRPC)', icon: <Layers size={14} color="#fff" /> },
          { name: 'Order Service (RM)', desc: 'gRPC rpc CreateOrder (Lưu tạm DB)', icon: <ShoppingCart size={14} color="#fff" /> },
          { name: 'Payment Service (RM)', desc: 'gRPC rpc ProcessPayment (Lưu tạm DB)', icon: <CreditCard size={14} color="#fff" /> },
          { name: 'Seata Server (TC)', desc: 'Commit / Rollback gRPC AT qua undo_log', icon: <ShieldCheck size={14} color="#fff" /> }
        ]
      case 'tcc':
        return [
          { name: 'Coordinator (TM)', desc: 'Bắt đầu TCC Transaction', icon: <Layers size={14} color="#fff" /> },
          { name: 'Order Service (TCC)', desc: 'Try (Pending) -> Confirm (Approve) / Cancel', icon: <ShoppingCart size={14} color="#fff" /> },
          { name: 'Payment Service (TCC)', desc: 'Try (Pending) -> Confirm (Success) / Cancel', icon: <CreditCard size={14} color="#fff" /> },
          { name: 'Seata Server (TC)', desc: 'Quản lý 2-Phase (Confirm / Cancel)', icon: <RefreshCcw size={14} color="#fff" /> }
        ]
      case 'saga-orch':
        return [
          { name: 'Coordinator (Orchestrator)', desc: 'Điều phối REST API tuần tự', icon: <Cpu size={14} color="#fff" /> },
          { name: 'Order Service', desc: 'Tạo order trạng thái PENDING', icon: <ShoppingCart size={14} color="#fff" /> },
          { name: 'Payment Service', desc: 'Xử lý thanh toán', icon: <CreditCard size={14} color="#fff" /> },
          { name: 'Bù trừ / Xác nhận', desc: 'Success -> APPROVED | Fail -> CANCELLED', icon: <CheckCircle2 size={14} color="#fff" /> }
        ]
      case 'saga-choreo':
        return [
          { name: 'Coordinator Service', desc: 'Kịch bản Choreography', icon: <Layers size={14} color="#fff" /> },
          { name: 'Order Service', desc: 'Tạo order, publish event sang Kafka', icon: <Network size={14} color="#fff" /> },
          { name: 'Payment Service', desc: 'Consume Kafka, xử lý, publish kết quả', icon: <CreditCard size={14} color="#fff" /> },
          { name: 'Kafka Topic Broker', desc: 'Định tuyến tin nhắn sự kiện toàn cục', icon: <GitBranch size={14} color="#fff" /> }
        ]
      default:
        return []
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setAlert(null)
    setConsoleLogs([])

    const addLog = (text, type = 'info') => {
      setConsoleLogs(prev => [...prev, {
        time: new Date().toTimeString().split(' ')[0] + '.' + String(new Date().getMilliseconds()).padStart(3, '0'),
        text,
        type
      }])
    }

    const steps = getFlowSteps()
    const currentStepStates = Array(steps.length).fill('pending')
    setStepStates([...currentStepStates])

    const url = `http://localhost:8083/api/coordinator/${mode}`
    const payload = {
      productId,
      quantity: parseInt(quantity),
      price: parseFloat(price),
      simulatePaymentError: simulateError
    }

    addLog(`[TM] Bắt đầu Giao dịch Toàn cục [Chế độ: ${mode.toUpperCase()}]`, 'info')
    addLog(`[TM] Tham số: ${JSON.stringify(payload)}`, 'info')

    // Step 0: Coordinator khởi tạo
    currentStepStates[0] = 'running'
    setStepStates([...currentStepStates])
    addLog(`[Coordinator] TM bắt đầu và đăng ký Transaction ID (XID) lên Seata Server (TC)...`, 'info')
    await new Promise(r => setTimeout(r, 600))
    currentStepStates[0] = 'success-step'
    setStepStates([...currentStepStates])
    addLog(`[Coordinator] Đăng ký XID thành công. Trạng thái: ACTIVE.`, 'success')
    await new Promise(r => setTimeout(r, 400))

    // Step 1: Order Service
    currentStepStates[1] = 'running'
    setStepStates([...currentStepStates])
    
    if (mode === 'grpc-at') {
      addLog(`[Order Service] [gRPC] Đang serialize dữ liệu Protobuf. Gọi RPC CreateOrder(CreateOrderRequest)...`, 'info')
    } else if (mode === 'saga-choreo') {
      addLog(`[Order Service] Khởi tạo đơn hàng tạm thời (PENDING) trong DB...`, 'info')
    } else {
      addLog(`[Order Service] Gửi REST POST đến /api/orders/pure-at để tạo Order...`, 'info')
    }
    await new Promise(r => setTimeout(r, 600))

    let response;
    let result;
    try {
      response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      })
      result = await response.json()
    } catch (err) {
      addLog(`[Error] Kết nối tới coordinator-service thất bại: ${err.message}`, 'error')
      currentStepStates[1] = 'failed-step'
      setStepStates([...currentStepStates])
      setAlert({
        type: 'danger',
        message: `Không thể kết nối đến coordinator-service: ${err.message}`
      })
      setLoading(false)
      fetchDatabases()
      return
    }

    const isSuccess = result.code === 200
    const orderId = result.data ? (result.data.id || result.data.orderId || 'N/A') : 'N/A'

    if (isSuccess) {
      // Step 1 Complete (Success)
      currentStepStates[1] = 'success-step'
      setStepStates([...currentStepStates])
      if (mode === 'grpc-at') {
        addLog(`[Order Service] [gRPC] Nhận OrderResponse. Order ID: ${orderId} (PENDING). Đã ghi nhận undo_log.`, 'success')
      } else if (mode === 'saga-choreo') {
        addLog(`[Order Service] Đã tạo Order ID: ${orderId} (PENDING).`, 'success')
        addLog(`[Order Service] [Kafka] Đang publish message 'ORDER_CREATED' sang Topic 'order-events' (Partition 0)...`, 'info')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Kafka Broker] Ghi event thành công: Topic='order-events', Payload={orderId: ${orderId}}`, 'success')
      } else if (mode === 'tcc') {
        addLog(`[Order Service] Phase 1 (Try) thành công. Đóng băng tài nguyên (Order ID: ${orderId}, PENDING).`, 'success')
      } else {
        addLog(`[Order Service] Đã tạo Order ID: ${orderId} thành công (PENDING). Đã tạo record undo_log.`, 'success')
      }
      await new Promise(r => setTimeout(r, 600))

      // Step 2: Payment Service
      currentStepStates[2] = 'running'
      setStepStates([...currentStepStates])
      if (mode === 'grpc-at') {
        addLog(`[Payment Service] [gRPC] Gọi RPC ProcessPayment(PaymentRequest) với số tiền: ${(quantity * price).toLocaleString()} đ...`, 'info')
      } else if (mode === 'saga-choreo') {
        addLog(`[Payment Service] [Kafka] Consume thành công 'ORDER_CREATED' từ Topic 'order-events'.`, 'info')
        await new Promise(r => setTimeout(r, 450))
        addLog(`[Payment Service] Đang tiến hành xử lý thanh toán tài khoản...`, 'info')
      } else if (mode === 'tcc') {
        addLog(`[Payment Service] Phase 1 (Try) đang chuẩn bị ví cho Order ID: ${orderId}...`, 'info')
      } else {
        addLog(`[Payment Service] Đang gọi REST POST /api/payments/at để thanh toán...`, 'info')
      }
      await new Promise(r => setTimeout(r, 600))

      currentStepStates[2] = 'success-step'
      setStepStates([...currentStepStates])
      if (mode === 'grpc-at') {
        addLog(`[Payment Service] [gRPC] Nhận gRPC PaymentResponse: SUCCESS. Đã ghi nhận undo_log.`, 'success')
      } else if (mode === 'saga-choreo') {
        addLog(`[Payment Service] Trừ ví thành công. Trạng thái Payment: SUCCESS.`, 'success')
        addLog(`[Payment Service] [Kafka] Đang publish message 'PAYMENT_SUCCESS' sang Topic 'payment-events'...`, 'info')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Kafka Broker] Ghi event thành công: Topic='payment-events', Payload={orderId: ${orderId}, status: 'SUCCESS'}`, 'success')
      } else if (mode === 'tcc') {
        addLog(`[Payment Service] Phase 1 (Try) thành công. Đóng băng số dư ví.`, 'success')
      } else {
        addLog(`[Payment Service] Thanh toán thành công (SUCCESS). Đã ghi nhận undo_log.`, 'success')
      }
      await new Promise(r => setTimeout(r, 600))

      // Step 3: TC Commit / Confirm / Finalize
      currentStepStates[3] = 'running'
      setStepStates([...currentStepStates])
      if (mode === 'grpc-at' || mode === 'at') {
        addLog(`[Seata TC] TM gửi yêu cầu COMMIT toàn cục. TC tiến hành commit tất cả các nhánh...`, 'info')
        await new Promise(r => setTimeout(r, 600))
        addLog(`[Seata TC] COMMIT toàn cục thành công! TC dọn sạch dữ liệu bảng undo_log của các RM.`, 'success')
      } else if (mode === 'tcc') {
        addLog(`[Seata TC] Phase 1 thành công. TC kích hoạt Phase 2 (Confirm) tới các dịch vụ...`, 'info')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Order Service] Gọi Confirm TCC -> Cập nhật trạng thái Order sang APPROVED. Giải phóng tài nguyên.`, 'success')
        await new Promise(r => setTimeout(r, 400))
        addLog(`[Payment Service] Gọi Confirm TCC -> Cập nhật trạng thái Payment sang SUCCESS.`, 'success')
      } else if (mode === 'saga-choreo') {
        addLog(`[Order Service] [Kafka] Consume thành công 'PAYMENT_SUCCESS' từ Topic 'payment-events'.`, 'info')
        await new Promise(r => setTimeout(r, 450))
        addLog(`[Order Service] Cập nhật trạng thái đơn hàng sang APPROVED...`, 'info')
        await new Promise(r => setTimeout(r, 400))
        addLog(`[Order Service] Cập nhật Order sang APPROVED thành công!`, 'success')
      } else if (mode === 'saga-orch') {
        addLog(`[Coordinator] Orchestrator gọi API cập nhật APPROVED cho Order...`, 'info')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Order Service] Trạng thái Order ID: ${orderId} đã cập nhật thành APPROVED.`, 'success')
      }
      
      currentStepStates[3] = 'success-step'
      setStepStates([...currentStepStates])
      addLog(`[TM] Giao dịch toàn cục hoàn tất thành công! Trạng thái: COMMITTED.`, 'success')

      setAlert({
        type: 'success',
        message: `${result.message}`
      })
    } else {
      // THẤT BẠI / ROLLBACK / BÙ TRỪ
      currentStepStates[1] = 'success-step'
      setStepStates([...currentStepStates])
      if (mode === 'grpc-at') {
        addLog(`[Order Service] [gRPC] Nhận OrderResponse. Order ID: ${orderId} (PENDING). Đã ghi nhận undo_log.`, 'success')
      } else if (mode === 'saga-choreo') {
        addLog(`[Order Service] Đã tạo Order ID: ${orderId} (PENDING).`, 'success')
        addLog(`[Order Service] [Kafka] Đang publish message 'ORDER_CREATED' sang Topic 'order-events'...`, 'info')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Kafka Broker] Ghi event thành công: Topic='order-events'`, 'success')
      } else if (mode === 'tcc') {
        addLog(`[Order Service] Phase 1 (Try) thành công. Đóng băng tài nguyên (Order ID: ${orderId}, PENDING).`, 'success')
      } else {
        addLog(`[Order Service] Đã tạo Order ID: ${orderId} (PENDING). Đã tạo record undo_log.`, 'success')
      }
      await new Promise(r => setTimeout(r, 600))

      // Step 2: Payment Service (Failed)
      currentStepStates[2] = 'running'
      setStepStates([...currentStepStates])
      if (mode === 'grpc-at') {
        addLog(`[Payment Service] [gRPC] Gọi RPC ProcessPayment(PaymentRequest)...`, 'info')
      } else if (mode === 'saga-choreo') {
        addLog(`[Payment Service] [Kafka] Consume 'ORDER_CREATED' từ Topic 'order-events'.`, 'info')
        await new Promise(r => setTimeout(r, 450))
        addLog(`[Payment Service] Đang tiến hành xử lý thanh toán...`, 'info')
      } else if (mode === 'tcc') {
        addLog(`[Payment Service] Phase 1 (Try) đang chuẩn bị ví cho Order ID: ${orderId}...`, 'info')
      } else {
        addLog(`[Payment Service] Đang gọi REST POST /api/payments/at để thanh toán...`, 'info')
      }
      await new Promise(r => setTimeout(r, 600))

      currentStepStates[2] = 'failed-step'
      setStepStates([...currentStepStates])
      if (mode === 'grpc-at') {
        addLog(`[Payment Service] [gRPC] [LỖI] Xử lý thanh toán thất bại! Throw gRPC StatusRuntimeException.`, 'error')
      } else if (mode === 'saga-choreo') {
        addLog(`[Payment Service] [LỖI] Giả lập lỗi thanh toán thất bại!`, 'error')
        addLog(`[Payment Service] [Kafka] Đang publish message 'PAYMENT_FAILED' sang Topic 'payment-events'...`, 'warning')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Kafka Broker] Ghi event thành công: Topic='payment-events', Payload={orderId: ${orderId}, status: 'FAILED'}`, 'success')
      } else if (mode === 'tcc') {
        addLog(`[Payment Service] [LỖI] Ví không đủ tiền hoặc lỗi ở bước TCC Prepare!`, 'error')
      } else {
        addLog(`[Payment Service] [LỖI] Xử lý thanh toán thất bại! Trả về HTTP 500.`, 'error')
      }
      await new Promise(r => setTimeout(r, 600))

      // Step 3: Rollback / Bù trừ
      currentStepStates[3] = 'running'
      setStepStates([...currentStepStates])
      
      if (mode === 'grpc-at' || mode === 'at') {
        addLog(`[Seata TC] Nhận thông báo lỗi. Kích hoạt ROLLBACK toàn cục...`, 'warning')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Order Service] Seata TC phát lệnh Rollback -> RM đọc undo_log, thực hiện SQL bù trừ để phục hồi...`, 'warning')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Order Service] Đã xóa bản ghi Order ID: ${orderId} trong DB. undo_log được giải phóng.`, 'success')
        await new Promise(r => setTimeout(r, 400))
        addLog(`[Seata TC] Rollback toàn cục hoàn tất thành công!`, 'success')
      } else if (mode === 'tcc') {
        addLog(`[Seata TC] Phát hiện lỗi Phase 1. Kích hoạt Phase 2 (Cancel) tới các dịch vụ...`, 'warning')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Order Service] Gọi Cancel TCC -> Hủy đơn hàng / Giải phóng tài nguyên đóng băng.`, 'success')
        await new Promise(r => setTimeout(r, 400))
        addLog(`[Payment Service] Gọi Cancel TCC -> Hủy thanh toán, giải phóng ví.`, 'success')
        await new Promise(r => setTimeout(r, 400))
        addLog(`[Seata TC] TCC Cancel hoàn tất!`, 'success')
      } else if (mode === 'saga-choreo') {
        addLog(`[Order Service] [Kafka] Consume 'PAYMENT_FAILED' từ Topic 'payment-events'.`, 'info')
        await new Promise(r => setTimeout(r, 450))
        addLog(`[Order Service] Phát hiện thanh toán lỗi qua Kafka. Kích hoạt giao dịch bù trừ (Compensate)...`, 'warning')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Order Service] Cập nhật trạng thái Order ID: ${orderId} sang CANCELLED để bù trừ.`, 'success')
      } else if (mode === 'saga-orch') {
        addLog(`[Coordinator] Nhận thông báo lỗi thanh toán. Kích hoạt quy trình bù trừ (Compensate)...`, 'warning')
        await new Promise(r => setTimeout(r, 500))
        addLog(`[Order Service] Gọi API bù trừ -> Cập nhật trạng thái Order ID: ${orderId} sang CANCELLED.`, 'success')
      }

      currentStepStates[3] = 'failed-step'
      setStepStates([...currentStepStates])
      addLog(`[TM] Giao dịch toàn cục thất bại. Trạng thái: ROLLED_BACK.`, 'error')

      setAlert({
        type: 'danger',
        message: `Giao dịch thất bại và đã được Rollback/Bù trừ: ${result.message}`
      })
    }

    setLoading(false)
    fetchDatabases()
  }

  return (
    <div className="container">
      <header style={{ alignItems: 'flex-start' }}>
        <div className="title-section">
          <h1>Global Transaction Coordinator Dashboard</h1>
          <p style={{ marginBottom: '0.5rem' }}>Hệ thống giám sát và điều phối giao dịch phân tán cho Microservices</p>
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <span className="badge badge-primary">
              <ShieldCheck size={14} style={{ marginRight: '4px' }} /> Seata Enabled
            </span>
            <span className="badge badge-primary">
              <GitBranch size={14} style={{ marginRight: '4px' }} /> Kafka Enabled
            </span>
            <span className="badge badge-success">Active</span>
          </div>
        </div>
        <div>
          <button 
            onClick={handleCleanAll} 
            className="btn-refresh" 
            style={{ 
              backgroundColor: 'rgba(239, 68, 68, 0.15)', 
              borderColor: 'rgba(239, 68, 68, 0.3)', 
              color: '#f87171',
              padding: '0.4rem 0.85rem',
              fontSize: '0.8rem',
              fontWeight: '600',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              marginTop: '0.5rem'
            }}
          >
            <AlertCircle size={14} /> Clean All Data
          </button>
        </div>
      </header>

      {alert && (
        <div className={`alert alert-${alert.type}`}>
          {alert.type === 'success' ? <CheckCircle2 size={16} /> : <AlertCircle size={16} />}
          <div>{alert.message}</div>
        </div>
      )}

      <div className="dashboard-grid">
        {/* Cột trái (360px) - Thao tác */}
        <div className="left-column">
          <div className="card">
            <h2 className="card-title">
              <Play size={16} color="#6366f1" /> Khởi tạo Giao dịch
            </h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Kịch bản Kiểm thử</label>
                <div className="mode-selector-grid">
                  <button 
                    type="button" 
                    className={`toggle-btn ${mode === 'at' ? 'active' : ''}`}
                    onClick={() => setMode('at')}
                  >
                    Seata AT (REST)
                  </button>
                  <button 
                    type="button" 
                    className={`toggle-btn ${mode === 'grpc-at' ? 'active' : ''}`}
                    onClick={() => setMode('grpc-at')}
                  >
                    Seata AT (gRPC)
                  </button>
                  <button 
                    type="button" 
                    className={`toggle-btn ${mode === 'tcc' ? 'active' : ''}`}
                    onClick={() => setMode('tcc')}
                  >
                    Seata TCC
                  </button>
                  <button 
                    type="button" 
                    className={`toggle-btn ${mode === 'saga-orch' ? 'active' : ''}`}
                    onClick={() => setMode('saga-orch')}
                  >
                    Saga Orchestration
                  </button>
                  <button 
                    type="button" 
                    className={`toggle-btn ${mode === 'saga-choreo' ? 'active' : ''}`}
                    onClick={() => setMode('saga-choreo')}
                  >
                    Saga Choreography (Kafka)
                  </button>
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

              <div style={{ display: 'flex', gap: '0.75rem' }}>
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

              <div className="form-group" style={{ margin: '1rem 0' }}>
                <label className="checkbox-label">
                  <input 
                    type="checkbox" 
                    checked={simulateError} 
                    onChange={(e) => setSimulateError(e.target.checked)}
                  />
                  <div className="checkbox-custom">
                    {simulateError && <span style={{ width: '8px', height: '8px', backgroundColor: '#fff', borderRadius: '1px' }} />}
                  </div>
                  <span>Giả lập lỗi thanh toán (Rollback/Bù trừ)</span>
                </label>
              </div>

              <button type="submit" className="btn-submit" disabled={loading}>
                {loading ? 'Đang điều phối...' : 'Kích hoạt Giao dịch'} <ArrowRight size={16} />
              </button>
            </form>
          </div>

          <div className="card">
            <h2 className="card-title">
              <GitBranch size={16} color="#10b981" /> Trực quan hóa luồng chạy
            </h2>
            <div className="visual-flow">
              {getFlowSteps().map((step, idx) => {
                const stepStatus = stepStates[idx] || 'pending';
                return (
                  <div className={`flow-node ${stepStatus}`} key={idx}>
                    <div className="node-icon">
                      {step.icon}
                    </div>
                    <div className="node-info">
                      <span className="node-name">{step.name}</span>
                      <span className="node-desc">{step.desc}</span>
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>

        {/* Cột phải - Console & Tables */}
        <div className="right-column">
          {/* Console Log */}
          <div className="card terminal-card">
            <div className="terminal-header">
              <h2 className="terminal-title" style={{ margin: 0, fontSize: '0.95rem' }}>
                <Terminal size={18} color="#a5b4fc" style={{ marginRight: '6px' }} /> Console Log
              </h2>
              <button 
                onClick={() => setConsoleLogs([])} 
                className="btn-refresh" 
                style={{ padding: '0.2rem 0.45rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px' }}
                title="Xóa Log"
              >
                <Trash2 size={12} /> Clear
              </button>
            </div>
            <div className="terminal-body">
              {consoleLogs.map((log, idx) => (
                <div className="terminal-line" key={idx}>
                  <span className="terminal-time">[{log.time}]</span>
                  <span className={`terminal-text ${log.type}`}>{log.text}</span>
                </div>
              ))}
              <div ref={terminalEndRef} />
            </div>
          </div>

          {/* Databases */}
          <div className="database-section-grid">
            <div className="card db-card">
              <div className="section-header">
                <h2>
                  <Database size={18} color="#6366f1" /> Order Service Database (Cổng 8081)
                </h2>
                <button className="btn-refresh" onClick={handleRefresh} disabled={refreshing}>
                  <RefreshCw size={14} className={refreshing ? 'refresh-spin' : ''} />
                </button>
              </div>
              <div className="table-wrapper">
                {orders.length === 0 ? (
                  <div className="empty-state">
                    <ShoppingCart size={28} />
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
                          <td 
                            style={{ 
                              fontSize: '0.72rem', 
                              fontFamily: 'monospace', 
                              color: 'var(--text-secondary)',
                              maxWidth: '120px',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap'
                            }} 
                            title={ord.txId || 'N/A'}
                          >
                            {ord.txId || 'N/A'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>

            <div className="card db-card">
              <div className="section-header">
                <h2>
                  <Database size={18} color="#10b981" /> Payment Service Database (Cổng 8082)
                </h2>
              </div>
              <div className="table-wrapper">
                {payments.length === 0 ? (
                  <div className="empty-state">
                    <CreditCard size={28} />
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
                          <td 
                            style={{ 
                              fontSize: '0.72rem', 
                              fontFamily: 'monospace', 
                              color: 'var(--text-secondary)',
                              maxWidth: '120px',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap'
                            }} 
                            title={pm.txId || 'N/A'}
                          >
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
    </div>
  )
}

export default App
