import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import "./Home.css";
import { jwtDecode } from "jwt-decode";
import { Card, Button, Text } from "@mantine/core";

const Home = ({ displayName }) => {
  const navigate = useNavigate();
  const [traderData, setTraderData] = useState(null);
  const [wallets, setWallets] = useState([]);
  const [pendingOrders, setPendingOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [transferAmount, setTransferAmount] = useState("");
  const [transferError, setTransferError] = useState(null);
  const [showTransferForm, setShowTransferForm] = useState(false);
  const [totalMarketValue, setTotalMarketValue] = useState(0);
  const [currentPrices, setCurrentPrices] = useState({});
  const [pnl, setPnl] = useState({ text: "", color: "white" });

  const symbols = ["BTCUSDT", "ETHUSDT"];

  const handleLogout = async () => {
    try {
      localStorage.clear();
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  const handleOrdersRedirect = () => {
    navigate("/orders");
  };

  const handlechartRedirect = () => {
    navigate("/");
  };

  useEffect(() => {
    const uid = localStorage.getItem("uid");
    const jwt = localStorage.getItem("jwt");
    const DataUrl = `${process.env.REACT_APP_TRADER_DATA_RUL}/${uid}`;

    if (jwt) {
      try {
        const decodedToken = jwtDecode(jwt);

        if (
          decodedToken.custom_claims &&
          decodedToken.custom_claims.includes("ADMIN")
        ) {
          navigate("/adminChart");
          return;
        }
      } catch (error) {
        console.error("Error decoding JWT:", error);
        setError("Invalid token. Please log in again.");
        setLoading(false);
        return;
      }
    } else {
      console.warn("JWT not found in localStorage");
      setLoading(false);
      setError("JWT not found in localStorage");
      return;
    }

    if (uid) {
      axios
        .get(DataUrl, {
          headers: {
            Authorization: `Bearer ${jwt}`,
          },
        })
        .then((response) => {
          setTraderData(response.data);
          localStorage.setItem("traderId", response.data.id);

          if (
            response.data.firstName === "haven't finished KYC" ||
            response.data.lastName === "haven't finished KYC"
          ) {
            navigate("/settings");
          }
        })
        .catch((err) => {
          console.error(
            "Error fetching trader data:",
            err.response ? err.response.data : err.message,
          );
          setError("Failed to fetch trader data");
          setLoading(false);
        });
    } else {
      console.warn("User ID not found in localStorage");
      setLoading(false);
      setError("User ID not found in localStorage");
    }
  }, []);

  const connectWebSocket = () => {
    const uid = localStorage.getItem("uid");
    const jwt = localStorage.getItem("jwt");

    const WEBSOCKET_URL = process.env.REACT_APP_WEBSOCKET_URL;
    const socket = new SockJS(WEBSOCKET_URL);

    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${jwt}`,
      },
      onConnect: () => {
        console.log("Connected to WebSocket.");

        client.subscribe(`/topic/orders/pending/${uid}`, (message) => {
          try {
            const ordersData = JSON.parse(message.body);
            setPendingOrders(ordersData);
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
      onStompError: (frame) => {
        console.error("Broker reported error:", frame.headers["message"]);
        setError("Failed to connect to WebSocket. Please try again.");
      },
      onWebSocketClose: () => {
        console.log("WebSocket connection closed.");
      },
      onWebSocketError: (error) => {
        console.error("WebSocket error:", error);
      },
    });

    client.activate();
  };

  useEffect(() => {
    if (traderData) {
      connectWebSocket();
    }

    return () => {};
  }, [traderData]);

  useEffect(() => {
    const WEBSOCKET_URL = process.env.REACT_APP_WEBSOCKET_URL;
    const socket = new SockJS(WEBSOCKET_URL);
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        symbols.forEach((symbol) => {
          client.subscribe(`/topic/currentPrice/${symbol}`, (message) => {
            try {
              const latestPrice = parseFloat(message.body);

              if (!isNaN(latestPrice)) {
                setCurrentPrices((prevPrices) => ({
                  ...prevPrices,
                  [symbol]: latestPrice,
                }));
              } else {
                console.error(
                  `Invalid price for ${symbol}:`,
                  message.body,
                );
              }
            } catch (error) {
              console.error("Error processing latest price:", error);
            }
          });
        });
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.activate();

    return () => client.deactivate();
  }, []);

  useEffect(() => {
    const totalO = pendingOrders.reduce((acc, order) => {
      const MarketValue = isNaN(currentPrices[order.currency])
        ? 1
        : order.buyAndSellType === "Sell"
        ? order.amount * currentPrices[order.currency]
        : order.amount * order.price;
      return acc + MarketValue;
    }, 0);

    const totalW = wallets.reduce((acc, wallet) => {
      const MarketValue =
        wallet.currencyAmount *
        (isNaN(currentPrices[wallet.currency])
          ? 1
          : currentPrices[wallet.currency]);
      return acc + MarketValue;
    }, 0);

    setTotalMarketValue(totalO + totalW);
  }, [pendingOrders, currentPrices]);

  const handleTransferUsdt = () => {
    if (
      !transferAmount ||
      isNaN(transferAmount) ||
      Number(transferAmount) <= 0
    ) {
      setTransferError("Please enter a valid amount.");
      return;
    }

    if (Number(transferAmount) > traderData.usdtBalance) {
      setTransferError("Insufficient balance.");
      return;
    }

    const traderId = localStorage.getItem("traderId");

    const requestBody = {
      traderId: Number(traderId),
      amount: Number(transferAmount).toFixed(2),
    };

    const jwt = localStorage.getItem("jwt");
    const TransferUrl = process.env.REACT_APP_TRANSFER;

    axios
      .post(TransferUrl, requestBody, {
        headers: {
          Authorization: `Bearer ${jwt}`,
          "Content-Type": "application/json",
        },
      })
      .then((response) => {
        alert(response.data);
        setTransferAmount("");
        setTransferError(null);
        setShowTransferForm(false);

        const uid = localStorage.getItem("uid");
        const DataUrl = `${process.env.REACT_APP_TRADER_DATA_RUL}/${uid}`;
        axios
          .get(DataUrl, {
            headers: {
              Authorization: `Bearer ${jwt}`,
            },
          })
          .then((response) => {
            setTraderData(response.data);
            localStorage.setItem("traderId", response.data.id);
          })
          .catch((err) => {
            console.error(
              "Error fetching updated trader data:",
              err.response ? err.response.data : err.message,
            );
            setError("Failed to fetch updated trader data");
          });
      })
      .catch((err) => {
        console.error("Transfer error:", err);
        setTransferError("Transfer failed. Please try again.");
      });
  };

  const handleMaxClick = () => {
    if (traderData) {
      setTransferAmount(traderData.usdtBalance.toString());
    }
  };

  useEffect(() => {
    const totalO = pendingOrders.reduce((acc, order) => {
      const MarketValue = isNaN(currentPrices[order.currency])
        ? 1
        : order.buyAndSellType === "Sell"
        ? order.amount * currentPrices[order.currency]
        : order.amount * order.price;
      return acc + MarketValue;
    }, 0);

    const totalW = wallets.reduce((acc, wallet) => {
      const MarketValue =
        wallet.currencyAmount *
        (isNaN(currentPrices[wallet.currency])
          ? 1
          : currentPrices[wallet.currency]);
      return acc + MarketValue;
    }, 0);

    const newTotalMarketValue = totalO + totalW;

    if (traderData) {
      if (traderData.yesterdayPrice === 0) {
        setPnl({
          value: "Today is your first day, good luck!",
          color: "white",
        });
      } else {
        const pnlValue = traderData.yesterdayPrice - newTotalMarketValue;
        setPnl({
          value: pnlValue.toFixed(2),
          color: pnlValue > 0 ? "green" : pnlValue < 0 ? "red" : "white",
        });
      }
    }
  }, [pendingOrders, currentPrices, traderData]);

  return (
    <div>
      {traderData ? (
        <h1 className="centered">Welcome {traderData.username}!</h1>
      ) : (
        <h1 className="centered">Welcome!</h1>
      )}
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
      <button onClick={handleOrdersRedirect}>Order records</button>
      <button
        onClick={handlechartRedirect}
        className={wallets.length > 0 ? "flashing-green-button" : ""}
      >
        Go to trading
      </button>
      <div className="card-container">
        <div className="transfer-button-container">
          {!showTransferForm ? (
            <button
              onClick={() => setShowTransferForm(true)}
              className={wallets.length === 0 ? "flashing-button" : ""}
            >
              Transfer USDT to Spot Account
            </button>
          ) : (
            <>
              <h4>Enter Amount to Transfer</h4>
              <div style={{ display: "flex", alignItems: "center" }}>
                <input
                  type="number"
                  value={transferAmount}
                  onChange={(e) => setTransferAmount(e.target.value)}
                  placeholder="Enter amount"
                />
                <button onClick={handleMaxClick} style={{ marginLeft: "10px" }}>
                  Max
                </button>
              </div>
              <button onClick={handleTransferUsdt}>Submit Transfer</button>
              {transferError && <p style={{ color: "red" }}>{transferError}</p>}
              <button onClick={() => setShowTransferForm(false)}>Cancel</button>
            </>
          )}
        </div>

        {traderData && (
          <Card
            shadow="sm"
            padding="lg"
            style={{ position: "absolute", top: 20, right: 20, width: 300 }}
          >
            <Text weight={500} size="lg" align="center">
              Trader Information
            </Text>
            <Text>
              <strong>ID:</strong> {traderData.id}
            </Text>
            <Text>
              <strong>Username:</strong> {traderData.username}
            </Text>
            <Text>
              <strong>Email:</strong> {traderData.email}
            </Text>
            <Text>
              <strong>First Name:</strong> {traderData.firstName}
            </Text>
            <Text>
              <strong>Last Name:</strong> {traderData.lastName}
            </Text>
            <Text>
              <strong>Phone Number:</strong> {traderData.phoneNumber}
            </Text>
            <Text
              style={{
                color: traderData.usdtBalance === 0 ? "inherit" : "red",
              }}
            >
              <strong>USDT Balance:</strong> {traderData.usdtBalance}
            </Text>
            <Text>
              <strong>Yesterday Market Value:</strong> $
              {traderData.yesterdayPrice.toFixed(2)}
            </Text>
          </Card>
        )}
      </div>

      {error && <p>{error}</p>}

      {traderData && (
        <div>
          <h3>Current Prices</h3>
          <ul>
            {symbols.map((symbol) => (
              <li key={symbol}>
                <strong>{symbol}:</strong> $
                {currentPrices[symbol]?.toFixed(2) || "Loading..."}
              </li>
            ))}
          </ul>

          {traderData && (
            <div>
              <h3 style={{ color: "white" }}>Today profit and loss:</h3>
              <p style={{ color: pnl.color }}>
                {typeof pnl.value === "string" ? pnl.value : `$${pnl.value}`}
              </p>
            </div>
          )}

          <h3>Your Wallets (Spot account)</h3>
          <table className="min-w-full border-collapse border border-gray-200">
            <thead>
              <tr>
                <th className="border border-gray-300 px-4 py-2">Currency</th>
                <th className="border border-gray-300 px-4 py-2">Balance</th>
                <th className="border border-gray-300 px-4 py-2">
                  Market Value
                </th>
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
                    <td className="border border-gray-300 px-4 py-2">
                      $
                      {wallet.currencyAmount *
                        (isNaN(currentPrices[wallet.currency])
                          ? 1
                          : currentPrices[wallet.currency])}
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
                <th className="border border-gray-300 px-4 py-2">Order Type</th>
                <th className="border border-gray-300 px-4 py-2">Buy/Sell</th>
                <th className="border border-gray-300 px-4 py-2">Currency</th>
                <th className="border border-gray-300 px-4 py-2">Price</th>
                <th className="border border-gray-300 px-4 py-2">Amount</th>
                <th className="border border-gray-300 px-4 py-2">Cost</th>
              </tr>
            </thead>
            <tbody>
              {pendingOrders.length > 0 ? (
                pendingOrders.map((order) => (
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
                      ${order.price.toFixed(2)}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">
                      {order.amount}
                    </td>
                    <td className="border border-gray-300 px-4 py-2">
                      $
                      {isNaN(currentPrices[order.currency])
                        ? 1
                        : order.buyAndSellType === "Sell"
                        ? order.amount * currentPrices[order.currency]
                        : order.amount * order.price}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td
                    colSpan="5"
                    className="border border-gray-300 px-4 py-2 text-center"
                  >
                    No pending orders found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default Home;
