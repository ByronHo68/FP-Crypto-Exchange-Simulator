import React, { useEffect, useState } from 'react';
import Chart from 'react-apexcharts';
import axios from 'axios';
import { useNavigate } from 'react-router-dom'; 

const popularSymbols = [
    'BTCUSDT',
    'ETHUSDT',
    'BNBUSDT',
    'XRPUSDT',
    'LTCUSDT',
    'ADAUSDT',
    'SOLUSDT',
    'DOGEUSDT',
    'DOTUSDT',
    'MATICUSDT',
];

const ApexCandleStick = () => {
    const [candlestickData, setCandlestickData] = useState([]);
    const [currentPrice, setCurrentPrice] = useState(0);
    const [symbol, setSymbol] = useState('BTCUSDT');
    const [error, setError] = useState('');
    const [inputSymbol, setInputSymbol] = useState('');
    const navigate = useNavigate();
    const [interval, setInterval] = useState('1m');

    const handleNavigation = (path) => {
        navigate(path);
    };

    const fetchInitialData = async (selectedInterval) => {
        const endTime = Date.now();
        let startTime;

        switch (selectedInterval) {
            case '1m':
                startTime = endTime - 120 * 60 * 1000; 
                break;
            case '5m':
                startTime = endTime - 120 * 5 * 60 * 1000; 
                break;
            case '1h':
                startTime = endTime - 120 * 60 * 60 * 1000; 
                break;
            case '1d':
                startTime = endTime - 120 * 24 * 60 * 60 * 1000; 
                break;
            default:
                startTime = endTime - 120 * 60 * 1000; 
        }

        try {
            const response = await axios.get('https://api.binance.com/api/v3/klines', {
                params: {
                    symbol: symbol,
                    interval: selectedInterval,
                    startTime: startTime,
                    endTime: endTime,
                    limit: 120,
                },
            });

            const formattedData = response.data.map(item => ({
                x: new Date(item[0]),
                y: [parseFloat(item[1]), parseFloat(item[2]), parseFloat(item[3]), parseFloat(item[4])],
            }));

            setCandlestickData(formattedData);
            if (formattedData.length > 0) {
                setCurrentPrice(parseFloat(formattedData[formattedData.length - 1].y[3]));
            }
            setError('');
        } catch (err) {
            setError('Invalid symbol. Please try again.');
        }
    };

    const handleIntervalChange = (newInterval) => {
        setInterval(newInterval);
        fetchInitialData(newInterval); 
    };

    useEffect(() => {
        fetchInitialData(interval); 
    
        const ws = new WebSocket(`wss://stream.binance.com:9443/ws/${symbol.toLowerCase()}@trade/${symbol.toLowerCase()}@kline_1m`);
    
        ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
        
            if (data.e === 'trade') {
            } else if (data.e === 'kline') {
                const candle = data.k;
                if (candle.x) {
                    setCandlestickData(prevData => [
                        ...prevData,
                        {
                            x: new Date(candle.t),
                            y: [parseFloat(candle.o), parseFloat(candle.h), parseFloat(candle.l), parseFloat(candle.c)],
                        },
                    ]);
                    setCurrentPrice(parseFloat(candle.c));
                }
            }
        };
        
        ws.onerror = (error) => {
            console.error('WebSocket error observed:', error);
            setError('WebSocket error: Unable to connect. Please check your internet connection or try again later.');
        };
    
        return () => {
            ws.close();
        };
    }, [symbol, interval]);

    const handleSymbolChange = (e) => {
        setSymbol(e.target.value);
        setInputSymbol('');
        setCandlestickData([]);
        setCurrentPrice(0);
    };

    const handleInputChange = (e) => {
        setInputSymbol(e.target.value.toUpperCase());
    };

    const handleSymbolSubmit = (e) => {
        e.preventDefault();
        const newSymbol = inputSymbol || symbol;
        setSymbol(newSymbol);
        setCandlestickData([]);
        setCurrentPrice(0);
    };


    const chartOptions = {
        chart: {
            type: 'candlestick',
            height: 350,
        },
        title: {
            text: 'Candlestick Chart',
            align: 'left',
        },
        xaxis: {
            type: 'datetime',
            labels: {
                format: 'dd MMM yyyy HH:mm:ss', 
            },
        },
        yaxis: {
            tooltip: {
                enabled: true,
            },
        },
        tooltip: {
            shared: true,
            intersect: false,
            x: {
                formatter: (value) => new Date(value).toLocaleString(), 
            },
        },
        annotations: {
            yaxis: [
                {
                    y: currentPrice,
                    borderColor: '#FF4560',
                    label: {
                        borderColor: '#FF4560',
                        style: {
                            color: '#fff',
                            background: '#FF4560',
                        },
                        text: `Current Price: $${currentPrice.toFixed(2)}`,
                    },
                },
            ],
        },
    };

    const buttonStyle = {
        padding: '15px 30px',
        fontSize: '16px', 
        color: '#fff', 
        backgroundColor: '#007BFF', 
        border: 'none', 
        borderRadius: '5px', 
        cursor: 'pointer', 
        transition: 'background-color 0.3s ease', 
    };

    const handleLogout = async () => {
        try {
            localStorage.clear();
            navigate('/');
            window.location.reload();
        } catch (error) {
            console.error('Logout error:', error);
        }
    };


    return (
        <div>
            <button 
    onClick={handleLogout} 
    style={{ backgroundColor: 'red', color: 'white', border: 'none', padding: '10px 20px', cursor: 'pointer' }}
>
    Logout
</button>
            <form onSubmit={handleSymbolSubmit}>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                    <select value={symbol} onChange={handleSymbolChange} style={{ marginRight: '10px' }}>
                        {popularSymbols.map((sym) => (
                            <option key={sym} value={sym}>
                                {sym}
                            </option>
                        ))}
                    </select>
                    <input
                        type="text"
                        value={inputSymbol}
                        onChange={handleInputChange}
                        placeholder="Or enter symbol (e.g., ETHUSDT)"
                        style={{ width: '400px', marginRight: '10px' }}
                    />
                    <button type="submit">Submit</button>
                </div>
            </form>
            {error && <p style={{ color: 'red' }}>{error}</p>}

            <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
             <button onClick={() => handleIntervalChange('1m')} style={{ ...buttonStyle, backgroundColor: '#4CAF50', color: 'white' }}>1 Minute</button>

<button onClick={() => handleIntervalChange('5m')} style={{ ...buttonStyle, backgroundColor: '#2196F3', color: 'white' }}>5 Minutes</button>

<button onClick={() => handleIntervalChange('1h')} style={{ ...buttonStyle, backgroundColor: '#FF9800', color: 'white' }}>Hourly</button>

<button onClick={() => handleIntervalChange('1d')} style={{ ...buttonStyle, backgroundColor: '#F44336', color: 'white' }}>Daily</button>
            </div>
            <Chart
                options={chartOptions}
                series={[{ data: candlestickData }]}
                type="candlestick"
                height={350}
                style={{
                    color: 'black',}}
            />
            <h2 style={{ marginTop: '20px' }}>Current Price: ${currentPrice.toFixed(2)}</h2>

            <button 
                    onClick={() => handleNavigation('/settingsA')} 
                    style={buttonStyle}
                >
                    Admin Settings
                </button>
                <button 
                    onClick={() => handleNavigation('/ordersA')} 
                    style={buttonStyle}
                >
                    Check All Orders 
                </button>
                <button 
                    onClick={() => handleNavigation('/walletsA')} 
                    style={buttonStyle}
                >
                    Check All Wallet
                </button>
                <button 
                    onClick={() => handleNavigation('/tradersA')} 
                    style={buttonStyle}
                >
                    Check All Traders
                </button>
        </div>
    );
};

export default ApexCandleStick;