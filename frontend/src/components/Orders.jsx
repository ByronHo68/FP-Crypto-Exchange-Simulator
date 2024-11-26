import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  flexRender,
} from "@tanstack/react-table";

const Orders = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");
  const [globalFilter, setGlobalFilter] = useState("");
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(30);
  let stompClient = null;

  const goToHomePage = () => {
    navigate("/home");
  };

  const connectWebSocket = () => {
    const uid = localStorage.getItem("uid");
    const jwt = localStorage.getItem("jwt");

    console.log("Connecting to WebSocket...");
    const WEBSOCKET_URL = process.env.REACT_APP_WEBSOCKET_URL;
    const socket = new SockJS(WEBSOCKET_URL);

    stompClient = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${jwt}`,
      },
      onConnect: (frame) => {
        console.log("Connected: " + frame);

        stompClient.subscribe(`/topic/orders/all/${uid}`, (message) => {
          console.log("Subscription successful for user:", uid);
          try {
            const ordersData = JSON.parse(message.body);
            console.log("Received Orders Data:", ordersData);
            setOrders(ordersData);
          } catch (error) {
            console.error("Error parsing orders data:", error);
          }
        });

        stompClient.publish({
          destination: `/app/orders/all/${uid}`,
          body: {},
        });
        console.log("Published request for orders with UID:", uid);
      },
      onStompError: (frame) => {
        console.error("Broker reported error: ", frame.headers["message"]);
        setError("Failed to connect to WebSocket. Please try again.");
      },
    });

    stompClient.activate();
  };

  useEffect(() => {
    connectWebSocket();

    return () => {
      if (stompClient) {
        stompClient.deactivate();
        console.log("Disconnected from WebSocket.");
      }
    };
  }, []);

  const columns = [
    { accessorKey: "traderId", header: "Trader ID" },
    { accessorKey: "marketOrLimitOrderTypes", header: "Order Type" },
    { accessorKey: "buyAndSellType", header: "Buy/Sell" },
    { accessorKey: "currency", header: "Currency" },
    { accessorKey: "price", header: "Price" },
    { accessorKey: "amount", header: "Amount" },
    { accessorKey: "orderStatus", header: "Status" },
    { accessorKey: "updateTime", header: "Update Time" },
  ];

  const table = useReactTable({
    data: orders,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    state: {
      globalFilter,
      pagination: {
        pageIndex,
        pageSize,
      },
    },
  });

  return (
    <div className="p-4">
      <h2 className="mt-4">Order Records</h2>
      {error && <div className="text-red-500">{error}</div>}
      <button onClick={goToHomePage} style={{ marginTop: "20px" }}>
        Back to Dashboard
      </button>

      <div className="mb-4">
        <input
          type="text"
          value={globalFilter}
          onChange={(e) => setGlobalFilter(e.target.value)}
          placeholder="Search all columns..."
          className="border border-gray-300 px-2 py-1"
        />
      </div>

      <table className="min-w-full border-collapse border border-gray-200">
        <thead>
          {table.getHeaderGroups().map((headerGroup) => (
            <tr key={headerGroup.id}>
              {headerGroup.headers.map((header) => (
                <th
                  key={header.id}
                  className="border border-gray-300 px-4 py-2"
                  onClick={header.column.getToggleSortingHandler()}
                >
                  {flexRender(
                    header.column.columnDef.header,
                    header.getContext(),
                  )}

                  {{
                    asc: " 🔼",
                    desc: " 🔽",
                  }[header.column.getIsSorted()] ?? null}
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody>
          {table.getRowModel().rows.length > 0 ? (
            table.getRowModel().rows.map((row) => (
              <tr key={row.id}>
                {row.getVisibleCells().map((cell) => (
                  <td
                    key={cell.id}
                    className="border border-gray-300 px-4 py-2"
                  >
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </td>
                ))}
              </tr>
            ))
          ) : (
            <tr>
              <td
                colSpan={columns.length}
                className="border border-gray-300 px-4 py-2 text-center"
              >
                No pending orders found.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <div className="mt-4 flex justify-between items-center">
        <span>
          Page{" "}
          <strong>
            {pageIndex + 1} of {table.getPageCount()}
          </strong>
        </span>
        <div>
          <button
            onClick={() => setPageIndex((old) => Math.max(old - 1, 0))}
            disabled={!table.getCanPreviousPage()}
            className="border border-gray-300 px-2 py-1 mr-2"
          >
            Previous
          </button>
          <button
            onClick={() =>
              setPageIndex((old) => Math.min(old + 1, table.getPageCount() - 1))
            }
            disabled={!table.getCanNextPage()}
            className="border border-gray-300 px-2 py-1"
          >
            Next
          </button>
        </div>
      </div>

      {table.getRowModel().rows.length === 0 && (
        <div className="text-center mt-4">No pending orders found.</div>
      )}
    </div>
  );
};

export default Orders;
