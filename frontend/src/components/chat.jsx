import React, { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import Chart from "react-apexcharts";
import { useNavigate } from "react-router-dom";
import { MantineProvider, Title, TextInput, Card, Button } from "@mantine/core";
import btcImage from "./BTCUSDT.png";
import ethImage from "./ETHUSDT.jpg";
import axios from "axios";
import { jwtDecode } from "jwt-decode";
import "./chat.css";


const WebSocketComponent = () => {
  const navigate = useNavigate();
  const [stompClient, setStompClient] = useState(null);
  const [candlestickData, setCandlestickData] = useState([]);
  const [currentPrice, setCurrentPrice] = useState(0);
  const [candlestickSubscription, setCandlestickSubscription] = useState(null);
  const [priceSubscription, setPriceSubscription] = useState(null);
  const [pendingOrders, setPendingOrders] = useState([]);
  const [wallets, setWallets] = useState([]);

  const [symbol, setSymbol] = useState("BTCUSDT");
  const [error, setError] = useState("");
  const [inputSymbol, setInputSymbol] = useState("");
  const [orderType, setOrderType] = useState("limit");
  const [buyOrSellType, setBuyOrSellType] = useState("Buy");
  const [price, setPrice] = useState(currentPrice);
  const [amount, setAmount] = useState(0);
  const [orderConfirmation, setOrderConfirmation] = useState("");
  const [showComponents, setShowComponents] = useState(false);
  const [showJWTComponents, setShowJWTComponents] = useState(false);
  const [loading, setLoading] = useState(true);
  const [usdtAmount, setUsdtAmount] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const chartOptions = {
    chart: {
      type: "candlestick",
    },
    xaxis: {
      type: "datetime",
      labels: {
        format: "HH:mm",
        datetimeUTC: false,
      },
      style: {
        colors: ["#ffffff"],
        fontSize: "12px",
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
        formatter: (value) =>
          new Date(value).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
          }),
      },
    },
    annotations: {
      yaxis: [
        {
          y: currentPrice,
          borderColor: "#FF4560",
          label: {
            borderColor: "#FF4560",
            style: {
              color: "#fff",
              background: "#FF4560",
            },

            text: `${currentPrice.toFixed(2)}`,
          },
        },
      ],
    },
  };

  useEffect(() => {
    const WEBSOCKET_URL = process.env.REACT_APP_WEBSOCKET_URL;
    const socket = new SockJS(WEBSOCKET_URL);
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: (frame) => {

        const candlestickSub = client.subscribe("/topic/candles", (message) => {
          try {
            const receivedCandles = JSON.parse(message.body);

            if (Array.isArray(receivedCandles)) {
              const formattedData = receivedCandles.map((item) => ({
                x: new Date(item.openTime),
                y: [
                  parseFloat(item.openPrice),
                  parseFloat(item.highPrice),
                  parseFloat(item.lowPrice),
                  parseFloat(item.closePrice),
                ],
              }));
              setCandlestickData(formattedData);
            } else if (receivedCandles && typeof receivedCandles === "object") {
              const formattedSingleCandle = [
                {
                  x: new Date(receivedCandles.openTime),
                  y: [
                    parseFloat(receivedCandles.openPrice),
                    parseFloat(receivedCandles.highPrice),
                    parseFloat(receivedCandles.lowPrice),
                    parseFloat(receivedCandles.closePrice),
                  ],
                },
              ];
              setCandlestickData((prevData) => [
                ...prevData,
                ...formattedSingleCandle,
              ]);
            } else {
              console.error(
                "Received candles are not in expected format:",
                receivedCandles,
              );
            }
          } catch (error) {
            console.error("Error parsing received candles:", error);
          }
        });
        setCandlestickSubscription(candlestickSub);

        client.onDisconnect = () => {
          if (candlestickSub) candlestickSub.unsubscribe();
          if (priceSubscription) priceSubscription.unsubscribe();
        };
      },
      onStompError: (frame) => {
        console.error("Broker reported error:", frame.headers["message"]);
      },
    });

    client.activate();
    setStompClient(client);

    return () => {
      client.deactivate();
    };
  }, []);

  const requestCandles = (symbol) => {
    if (stompClient && stompClient.connected) {
      setSymbol(symbol);
      setCandlestickData([]);

      setUsdtAmount(0);
      setAmount(0);
      setOrderType("limit");
      setPrice(0);

      if (candlestickSubscription) {
        candlestickSubscription.unsubscribe();
        setCandlestickSubscription(null);
        console.log("Unsubscribed from previous candlestick subscription");
      }

      if (priceSubscription) {
        priceSubscription.unsubscribe();
        setPriceSubscription(null);
        console.log("Unsubscribed from previous price subscription");
      }

      stompClient.publish({
        destination: "/app/candles",
        body: symbol,
      });

      const priceSub = stompClient.subscribe(
        `/topic/currentPrice/${symbol}`,
        (message) => {
          try {
            const latestPrice = parseFloat(message.body);
            setCurrentPrice(latestPrice);
          } catch (error) {
            console.error("Error parsing latest price:", error);
          }
        },
      );

      setPriceSubscription(priceSub);

      const candlestickSub = stompClient.subscribe(
        `/topic/candles/${symbol}`,
        (message) => {
          try {
            const receivedCandles = JSON.parse(message.body);

            if (Array.isArray(receivedCandles)) {
              const formattedData = receivedCandles.map((item) => ({
                x: new Date(item.openTime),
                y: [
                  parseFloat(item.openPrice),
                  parseFloat(item.highPrice),
                  parseFloat(item.lowPrice),
                  parseFloat(item.closePrice),
                ],
              }));

              setCandlestickData(formattedData);
            } else if (receivedCandles && typeof receivedCandles === "object") {
              const formattedSingleCandle = [
                {
                  x: new Date(receivedCandles.openTime),
                  y: [
                    parseFloat(receivedCandles.openPrice),
                    parseFloat(receivedCandles.highPrice),
                    parseFloat(receivedCandles.lowPrice),
                    parseFloat(receivedCandles.closePrice),
                  ],
                },
              ];

              setCandlestickData((prevData) => [
                ...prevData,
                ...formattedSingleCandle,
              ]);
            } else {
              console.error(
                "Received candles for symbol are not in expected format:",
                receivedCandles,
              );
            }
          } catch (error) {
            console.error("Error parsing received candles for symbol:", error);
          }
        },
      );

      setCandlestickSubscription(candlestickSub);
      setShowComponents(true);
    } else {
      console.error("STOMP client is not connected.");
    }
  };
  const connectWebSocket = () => {
    const uid = localStorage.getItem("uid");
    const jwt = localStorage.getItem("jwt");

    if (jwt) {
      try {
        const decodedToken = jwtDecode(jwt);
        setShowJWTComponents(true);

        if (
          decodedToken.custom_claims &&
          decodedToken.custom_claims.includes("ADMIN")
        ) {
          console.log("User is an admin. Redirecting to /adminChart");
          navigate("/adminChart");
          return;
        }
      } catch (error) {
        console.error("Error decoding JWT:", error);
        setError("Invalid token. Please log in again.");
        setLoading(false);
        setShowJWTComponents(false);
        return;
      }
    } else {
      console.warn("JWT not found in localStorage");
      setLoading(false);
      setError("JWT not found in localStorage");
      setShowJWTComponents(false);
      return;
    }

    const WEBSOCKET_URL = process.env.REACT_APP_WEBSOCKET_URL;
    const socket = new SockJS(WEBSOCKET_URL);

    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${jwt}`,
      },
      onConnect: () => {

        client.subscribe(`/topic/orders/pending/${uid}`, (message) => {
          try {
            const data = JSON.parse(message.body);

            if (Array.isArray(data)) {
              setPendingOrders(data);
            } else {
              const orderId = data.id;

              setPendingOrders((prevOrders) => {
                if (!prevOrders) {
                  console.warn(
                    "prevOrders is undefined, initializing to empty array.",
                  );
                  return [data];
                }

                const existingOrderIndex = prevOrders.findIndex(
                  (order) => order.id === orderId,
                );
                if (existingOrderIndex !== -1) {
                  return prevOrders.filter((order) => order.id !== orderId);
                } else {
                  return [...prevOrders, data];
                }
              });
            }
          } catch (error) {
            console.error("Error parsing pending orders data:", error);
          }
        });
        client.subscribe(`/topic/wallets/${uid}`, (message) => {
          try {
            const walletsData = JSON.parse(message.body);

            if (Array.isArray(walletsData)) {
              setWallets(walletsData);
            } else if (walletsData && typeof walletsData === "object") {
              setWallets((prevWallets) => {
                const walletCurrency = walletsData.currency;
                const walletAmount = walletsData.amount;

                const updatedWallets = prevWallets.map((wallet) => {
                  if (wallet.currency === walletCurrency) {
                    return {
                      ...wallet,
                      currencyAmount: walletAmount,
                    };
                  }
                  return wallet;
                });

                const currencyExists = updatedWallets.some(
                  (wallet) => wallet.currency === walletCurrency,
                );

                if (!currencyExists) {
                  updatedWallets.push({
                    currency: walletCurrency,
                    currencyAmount: walletAmount,
                    id: walletsData.id,
                    createdAt: walletsData.createdAt,
                    trader: walletsData.trader,
                  });
                }
                return updatedWallets;
              });
            }
          } catch (error) {
            console.error("Error parsing wallets data:", error);
          }
        });
        client.publish({
          destination: `/app/orders/pending/${uid}`,
          body: JSON.stringify({}),
        });
        client.publish({
          destination: `/app/wallets/update/${uid}`,
          body: JSON.stringify({}),
        });
      },
    });

    client.activate();
  };

  useEffect(() => {
    connectWebSocket();

    return () => {};
  }, []);

  const deleteOrder = async (orderId) => {
    const token = localStorage.getItem("jwt");
    const deleteUrl = `${process.env.REACT_APP_ORDER}/${orderId}`;

    try {
      await axios.delete(deleteUrl, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setOrderConfirmation(`Order ID ${orderId} deleted successfully.`);
    } catch (error) {
      console.error("Error deleting order:", error);
      setError("Failed to delete order. Please try again.");
    }
  };
  const handleSymbolChange = (e) => {
    setSymbol(e.target.value);
    setInputSymbol("");
    setCandlestickData([]);
    setCurrentPrice(0);
  };
  const handleOrderTypeChange = (e) => {
    const selectedOrderType = e.target.value;
    setOrderType(selectedOrderType);

    if (selectedOrderType === "market") {
      setPrice(currentPrice);
    }
  };
  const handleBuyOrSellChange = () => {
    setBuyOrSellType((prev) => (prev === "Buy" ? "Sell" : "Buy"));
    setUsdtAmount(0);
  };
  const handleMaxAmountClick = () => {
    if (buyOrSellType === "Sell") {
      const wallet = wallets.find((wallet) => wallet.currency === symbol);
      if (wallet) {
        setAmount(wallet.currencyAmount);
        setUsdtAmount(wallet.currencyAmount * price);
      } else {
        setError("Insufficient balance in wallet.");
      }
    } else if (buyOrSellType === "Buy") {
      const usdtWallet = wallets.find((wallet) => wallet.currency === "USDT");
      if (orderType === "limit") {
        const maxUsdtAmount = usdtWallet.currencyAmount;
        setUsdtAmount(maxUsdtAmount);
        setAmount((maxUsdtAmount / price).toFixed(8));
      } else {
        const maxUsdtAmount = usdtWallet.currencyAmount;
        setUsdtAmount(maxUsdtAmount);
        setAmount((maxUsdtAmount / currentPrice).toFixed(8));
      }
    }
  };
  const handleSymbolSubmit = async (e) => {
    e.preventDefault();
    if (isSubmitting) return;
    setIsSubmitting(true);

    if (amount <= 0 || usdtAmount <= 0) {
      setError("Amount must be greater than 0.");
      setIsSubmitting(false);
      setTimeout(() => setError(""), 3000);
      return;
    }
    if (price <= 0) {
      setError("Price must be greater than 0.");
      setIsSubmitting(false);
      setTimeout(() => setError(""), 3000);
      return;
    }

    if (buyOrSellType === "Buy") {
      const usdtWallet = wallets.find((wallet) => wallet.currency === "USDT");

      if (!usdtWallet || usdtWallet.currencyAmount < usdtAmount) {
        setError("Insufficient USDT balance for this buy order.");
        setIsSubmitting(false);
        setTimeout(() => setError(""), 3000);
        return;
      }
    } else {
      const currencyWallet = wallets.find(
        (wallet) => wallet.currency === symbol,
      );

      if (!currencyWallet || currencyWallet.currencyAmount < amount) {
        setError(`Insufficient ${symbol} balance for this sell order.`);
        setIsSubmitting(false);
        setTimeout(() => setError(""), 3000);
        return;
      }
    }

    const orderDetails = {
      traderId: localStorage.getItem("traderId"),
      marketOrLimitOrderTypes: orderType,
      buyAndSellType: buyOrSellType,
      currency: symbol,
      price: orderType === "market" ? currentPrice : price,
      amount: parseFloat(amount),
    };

    try {
      const token = localStorage.getItem("jwt");
      const createUrl = process.env.REACT_APP_ORDER;
      const response = await axios.post(createUrl, orderDetails, {
        headers: { Authorization: `Bearer ${token}` },
      });

      console.log("Order created successfully:", response.data);
      setUsdtAmount(0);
      setAmount(0);

      setOrderConfirmation(
        `Order placed successfully! Order ID: ${response.data.id}, ${symbol} Amount: ${amount}, Type: ${buyOrSellType}`,
      );
      const timeoutId = setTimeout(() => {
        setOrderConfirmation("");
      }, 3000);
      return () => clearTimeout(timeoutId);
    } catch (error) {
      console.error("Error creating order:", error.response.data);

      if (
        error.response &&
        error.response.data &&
        error.response.data.message
      ) {
        setError(error.response.data.message);
      } else {
        setError("Failed to create order. Please try again.");
      }

      setOrderConfirmation("");

      const timeoutId = setTimeout(() => {
        setError("");
      }, 3000);

      return () => clearTimeout(timeoutId);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleLogout = async () => {
    try {
      localStorage.clear();
      navigate("/");
      window.location.reload();
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  const handleLoginRedirect = () => {
    navigate("/login");
  };

  const cardBackgroundColor = buyOrSellType === "Buy" ? "#1B5E20" : "#C62828";

  return (
    <MantineProvider theme={{ colorScheme: "dark" }}>
      <Title order={2} align="center">
        {showComponents
          ? `${symbol} Candle Stick Chart`
          : "Welcome to the Trading Apps! Please choose the cryptocurrency"}
      </Title>
      {!showJWTComponents && (
        <>
          <button onClick={handleLoginRedirect}>Login/Signup</button>
        </>
      )}
      {showJWTComponents && (
        <>
          <button
            onClick={handleLogout}
            style={{
              backgroundColor: "red",
              color: "white",
              border: "none",
              padding: "10px 20px",
              cursor: "pointer",
            }}
          >
            Logout
          </button>
        </>
      )}
      <div>
        <div
          style={{
            display: "flex",
            justifyContent: "flex-end",
            margin: "20px 0",
          }}
        >
          {showJWTComponents && (
            <>
              <button
                onClick={() => navigate("/home")}
                style={{ marginTop: "20px" }}
              >
                Go to Dashboard
              </button>
            </>
          )}
        </div>
        <h1> </h1>

        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            margin: "20px 0",
          }}
        >
          <button
            onClick={() => requestCandles("ETHUSDT")}
            style={{
              backgroundImage: `url(${ethImage})`,
              backgroundSize: "contain",
              backgroundRepeat: "no-repeat",
              width: "50px",
              height: "50px",
              border: "none",
              cursor: "pointer",
              marginRight: "10px",
              borderRadius: "50%",
            }}
          />

          <button
            onClick={() => requestCandles("BTCUSDT")}
            style={{
              backgroundImage: `url(${btcImage})`,
              backgroundSize: "contain",
              backgroundRepeat: "no-repeat",
              width: "50px",
              height: "50px",
              border: "none",
              cursor: "pointer",
              marginRight: "10px",
              borderRadius: "50%",
            }}
          />
        </div>

        {showComponents && (
          <>
            <Chart
              options={chartOptions}
              series={[{ data: candlestickData }]}
              type="candlestick"
              height={350}
              style={{
                color: "black",
              }}
            />
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                marginTop: "20px",
                height: "15vh",
              }}
            >
              <h2 className="mt-4">
                Current Price: ${currentPrice.toFixed(8)}
              </h2>
            </div>
            <h1> </h1>
          </>
        )}
        {showJWTComponents && (
          <>
            {showJWTComponents && showComponents && (
              <>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    height: "15vh",
                  }}
                >
                  <form
                    onSubmit={handleSymbolSubmit}
                    className="flex flex-col mb-4"
                  >
                    <Card
                      shadow="sm"
                      padding="lg"
                      radius="md"
                      withBorder
                      style={{
                        backgroundColor: cardBackgroundColor,
                        border: "1px solid #444444",
                        width: "100%",
                        maxWidth: "600px",
                        height: "20%",
                        maxHeight: "300px",
                        margin: "0 auto",
                      }}
                    >
                      {orderConfirmation && (
                        <div
                          style={{
                            color: "green",
                            marginTop: "1px",
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                          }}
                        >
                          <p>{orderConfirmation}</p>
                        </div>
                      )}
                      <div
                        className="flex mb-2 align-items-center"
                        style={{
                          marginTop: "1px",
                          display: "flex",
                          justifyContent: "center",
                          alignItems: "center",
                          height: "5vh",
                        }}
                      >
                        <span
                          className="toggle-text"
                          style={{ marginRight: "10px" }}
                        >
                          Sell
                        </span>
                        <div className="toggle-container">
                          <input
                            type="checkbox"
                            id="buySellToggle"
                            className="toggle-input"
                            checked={buyOrSellType === "Buy"}
                            onChange={handleBuyOrSellChange}
                          />
                          <label
                            htmlFor="buySellToggle"
                            className="toggle-label"
                          >
                            <span className="slider"></span>
                          </label>
                        </div>
                        <span
                          className="toggle-text"
                          style={{ marginLeft: "10px" }}
                        >
                          Buy
                        </span>
                      </div>

                      <div
                        className="flex mb-2"
                        style={{
                          display: "flex",
                          justifyContent: "center",
                          alignItems: "center",
                        }}
                      >
                        <span
                          className="toggle-text"
                          style={{ marginRight: "10px" }}
                        >
                          Market
                        </span>
                        <div className="toggle-container">
                          <input
                            type="checkbox"
                            id="orderTypeToggle"
                            className="toggle-input"
                            checked={orderType === "limit"}
                            onChange={(e) => {
                              const newOrderType = e.target.checked
                                ? "limit"
                                : "market";
                              setOrderType(newOrderType);
                              handleOrderTypeChange({
                                target: { value: newOrderType },
                              });
                            }}
                          />
                          <label
                            htmlFor="orderTypeToggle"
                            className="toggle-label"
                          >
                            <span className="slider"></span>
                          </label>
                        </div>
                        <span
                          className="toggle-text"
                          style={{ marginLeft: "10px" }}
                        >
                          Limit
                        </span>
                      </div>

                      {orderType === "limit" && (
                        <div
                          className="flex mb-2"
                          style={{
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                            height: "5vh",
                          }}
                        >
                          <label className="mb-1 font-semibold">
                            Limit Price:
                          </label>
                          <TextInput
                            type="number"
                            value={price}
                            onChange={(e) => setPrice(e.target.value)}
                            placeholder="Enter limit price"
                            className="mb-2 p-2 border rounded"
                            step="0.01"
                          />
                        </div>
                      )}
                      <div
                        className="flex mb-2"
                        style={{
                          display: "flex",
                          justifyContent: "center",
                          alignItems: "center",
                          height: "3vh",
                        }}
                      >
                        <label className="mb-1 font-semibold">
                          USDT Amount:
                        </label>
                        <TextInput
                          type="number"
                          value={usdtAmount}
                          onChange={(e) => {
                            const value = parseFloat(e.target.value);
                            setUsdtAmount(value);

                            if (orderType === "limit") {
                              if (buyOrSellType === "Buy") {
                                setAmount((value / price).toFixed(8));
                              } else {
                                setAmount((value / price).toFixed(8));
                              }
                            } else {
                              if (buyOrSellType === "Buy") {
                                setAmount((value / currentPrice).toFixed(8));
                              } else {
                                setAmount((value / currentPrice).toFixed(8));
                              }
                            }
                          }}
                          placeholder="Enter USDT amount"
                          className="p-2 border rounded mb-2"
                        />

                        <label className="mb-1 font-semibold">
                          {" "}
                          {symbol.substring(0, 3)} Amount:
                        </label>
                        <div className="flex items-center mb-2"></div>
                        <div
                          className="flex mb-2"
                          style={{
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                          }}
                        >
                          <TextInput
                            type="number"
                            value={amount}
                            onChange={(e) => {
                              const value = parseFloat(e.target.value);
                              setAmount(value);

                              if (orderType === "limit") {
                                if (buyOrSellType === "Buy") {
                                  setUsdtAmount((value * price).toFixed(8));
                                } else {
                                  setUsdtAmount((value * price).toFixed(8));
                                }
                              } else {
                                if (buyOrSellType === "Buy") {
                                  setUsdtAmount(
                                    (value * currentPrice).toFixed(8),
                                  );
                                } else {
                                  setUsdtAmount(
                                    (value * currentPrice).toFixed(8),
                                  );
                                }
                              }
                            }}
                            placeholder="Enter crypto amount"
                            className="p-2 border rounded mb-2"
                          />
                          <button
                            type="button"
                            onClick={handleMaxAmountClick}
                            className="ml-2 p-1 border rounded bg-blue-500 text-white"
                            style={{ width: "60px" }}
                          >
                            Max
                          </button>
                        </div>
                      </div>

                      <div
                        style={{
                          display: "flex",
                          justifyContent: "center",
                          alignItems: "center",
                        }}
                      >
                        <button
                          type="submit"
                          onClick={handleSymbolSubmit}
                          disabled={isSubmitting}
                          className="ml-2 p-1 border rounded bg-blue-500 text-white"
                        >
                          {isSubmitting ? "Submitting..." : "Submit Order"}
                        </button>
                      </div>

                      {error && <p style={{ color: "#ff4d4d" }}>{error}</p>}
                    </Card>
                  </form>
                </div>
              </>
            )}

            <h3>Your Wallets</h3>
            <table className="min-w-full border-collapse border border-gray-200">
              <thead>
                <tr>
                  <th className="border border-gray-300 px-4 py-2">Currency</th>
                  <th className="border border-gray-300 px-4 py-2">Balance</th>
                </tr>
              </thead>
              <tbody>
                {wallets.length > 0 ? (
                  wallets.map((wallet) => (
                    <tr key={wallet.currencyWalletId}>
                      <td className="border border-gray-300 px-4 py-2">
                        {wallet.currency}
                      </td>
                      <td className="border border-gray-300 px-4 py-2">
                        {wallet.currencyAmount}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td
                      colSpan="3"
                      className="border border-gray-300 px-4 py-2 text-center"
                    >
                      No wallets found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>

            <h3>Your Pending Orders</h3>
            <table className="min-w-full border-collapse border border-gray-200">
              <thead>
                <tr>
                  <th className="border border-gray-300 px-4 py-2">
                    Order Type
                  </th>
                  <th className="border border-gray-300 px-4 py-2">Buy/Sell</th>
                  <th className="border border-gray-300 px-4 py-2">Currency</th>
                  <th className="border border-gray-300 px-4 py-2">Price</th>
                  <th className="border border-gray-300 px-4 py-2">Amount</th>
                  <th className="border border-gray-300 px-4 py-2">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pendingOrders.length > 0 ? (
                  pendingOrders.map((order) =>
                    order ? (
                      <tr key={order.id}>
                        <td className="border border-gray-300 px-4 py-2">
                          {order.marketOrLimitOrderTypes}
                        </td>
                        <td className="border border-gray-300 px-4 py-2">
                          {order.buyAndSellType}
                        </td>
                        <td className="border border-gray-300 px-4 py-2">
                          {order.currency}
                        </td>
                        <td className="border border-gray-300 px-4 py-2">
                          ${order.price ? order.price.toFixed(8) : "N/A"}
                        </td>{" "}
                        {/* Check price existence */}
                        <td className="border border-gray-300 px-4 py-2">
                          {order.amount}
                        </td>
                        <td className="border border-gray-300 px-4 py-2">
                          <button
                            onClick={() => deleteOrder(order.id)}
                            style={{
                              backgroundColor: "#ff6b6b",
                              color: "#ffffff",
                              padding: "5px 10px",
                              borderRadius: "5px",
                              cursor: "pointer",
                            }}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ) : null,
                  )
                ) : (
                  <tr>
                    <td
                      colSpan="6"
                      className="border border-gray-300 px-4 py-2 text-center"
                    >
                      No pending orders found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </>
        )}
      </div>
    </MantineProvider>
  );
};

export default WebSocketComponent;
