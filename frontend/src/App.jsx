import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'; // Import BrowserRouter here
import { MantineProvider } from '@mantine/core';
import Login from './components/Login';
import Signup from './components/Signup';
import Home from './components/Home';
import Settings from './components/Settings'; // Import the Settings component
import Orders from './components/Orders'; // Import the Orders component
import Chat from './components/chat';
import SettingsA from './components/SettingsA'; 
import OrdersA from './components/OrdersA';
import WalletA from './components/WalletsA';
import TradersA from './components/TradersA';
import AdminChart from './components/AdminChart';
import './styles.css';

const App = () => {
    return (
        <MantineProvider withGlobalStyles withNormalizeCSS>
            <Router>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/signup" element={<Signup />} />
                    <Route path="/home" element={<Home />} />
                    <Route path="/settings" element={<Settings />} />
                    <Route path="/orders" element={<Orders />} />
                    <Route path="/" element={<Chat />} />
                    <Route path="/settingsA" element={<SettingsA />} />
                    <Route path="/ordersA" element={<OrdersA />} />
                    <Route path="/walletsA" element={<WalletA />} />
                    <Route path='/tradersA' element={<TradersA/>} />
                    <Route path='/adminChart' element={<AdminChart/>} />
                </Routes>
            </Router>
        </MantineProvider>
    );
};

export default App;