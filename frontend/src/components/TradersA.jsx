import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  flexRender,
} from "@tanstack/react-table";

const Traders = () => {
  const navigate = useNavigate();
  const [traders, setTraders] = useState([]);
  const [error, setError] = useState("");
  const [globalFilter, setGlobalFilter] = useState("");
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(30);

  const goToHomePage = () => {
    navigate("/home");
  };

  const fetchTraders = async () => {
    const uid = localStorage.getItem("uid");
    const token = localStorage.getItem("jwt");
    const url = process.env.REACT_APP_ADMIN_TRADERS_URL;

    try {
      const response = await axios.get(url, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const transformedTraders = response.data.map((trader) => ({
        ...trader,
        createdAt: formatDate(trader.createdAt),
        updatedAt: formatDate(trader.updatedAt),
      }));
      setTraders(transformedTraders);
      console.log("Fetched Traders Data:", transformedTraders);
    } catch (error) {
      console.error("Error fetching traders:", error);
      setError("Failed to fetch traders. Please try again.");
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16);
  };

  useEffect(() => {
    fetchTraders();
  }, []);

  const columns = [
    { accessorKey: "id", header: "Trader ID", minSize: 100 },
    { accessorKey: "username", header: "Username", minSize: 150 },
    { accessorKey: "email", header: "Email", minSize: 200 },
    { accessorKey: "firstName", header: "First Name", minSize: 150 },
    { accessorKey: "lastName", header: "Last Name", minSize: 150 },
    { accessorKey: "createdAt", header: "Created At", minSize: 150 },
    { accessorKey: "updatedAt", header: "Updated At", minSize: 150 },
    { accessorKey: "traderNumber", header: "Trader Number", minSize: 150 },
    { accessorKey: "usdtBalance", header: "USDT Balance", minSize: 150 },
    { accessorKey: "idNumber", header: "ID Number", minSize: 150 },
    { accessorKey: "phoneNumber", header: "Phone Number", minSize: 150 },
    { accessorKey: "yesterdayPrice", header: "Yesterday Price", minSize: 150 },
  ];

  const table = useReactTable({
    data: traders,
    columns,
    enableColumnResizing: true,
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
      <h2 className="mt-4">Trader Records</h2>
      {error && <div className="text-red-500">{error}</div>}
      <button onClick={goToHomePage} style={{ marginTop: "20px" }}>
        Back to Home Page
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
                  className="border border-gray-300 px-4 py-2 relative"
                  onClick={header.column.getToggleSortingHandler()}
                  style={{ minWidth: header.column.columnDef.minSize }}
                >
                  {flexRender(
                    header.column.columnDef.header,
                    header.getContext(),
                  )}

                  {{
                    asc: " 🔼",
                    desc: " 🔽",
                  }[header.column.getIsSorted()] ?? null}
                  <span
                    className="absolute right-0 top-0 h-full w-1 cursor-col-resize"
                    style={{ width: "5px" }}
                  />
                </th>
              ))}
            </tr>
          ))}
        </thead>
        <tbody>
          {table.getRowModel().rows.map((row) => (
            <tr key={row.id}>
              {row.getVisibleCells().map((cell) => (
                <td key={cell.id} className="border border-gray-300 px-4 py-2">
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </td>
              ))}
            </tr>
          ))}
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
        <div className="text-center mt-4">No traders found.</div>
      )}
    </div>
  );
};

export default Traders;
