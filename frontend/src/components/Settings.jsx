import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { MantineProvider, Card, Title, TextInput, Button, Group, Text } from '@mantine/core';

const Settings = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        userId: '',
        traderNumber: '',
        usdtNalance: 0,
        idNumber: '',
        phoneNumber: '',
        yesterdayPrice: 0,
    });

    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [modalOpened, setModalOpened] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [modalMessage, setModalMessage] = useState('');
    const [countdown, setCountdown] = useState(3); 

    useEffect(() => {
        const jwt = localStorage.getItem('jwt');
        const displayName = localStorage.getItem('displayName') || '';
        const uid = localStorage.getItem('uid') || '';
        const email = jwt ? JSON.parse(atob(jwt.split('.')[1])).email : '';
        

        setFormData((prevData) => ({
            ...prevData,
            username: displayName,
            email: email,
            userId: uid,
        }));
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;

        if (name === 'usdtBalance') {
            const numericValue = parseFloat(value);
            if (numericValue > 9999999) {
                setError('USDT Balance cannot exceed 9,999,999.');
                return;
            }
        }

        setFormData({
            ...formData,
            [name]: value,
        });
        setError('');
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
    
        if (name === 'phoneNumber') {
            if (!validatePhoneNumber(value)) {
                setError('Invalid phone number format. Please use xxx-xxx-xxxx or similar.');
                return; 
            }
        }
    
        setFormData({
            ...formData,
            [name]: value,
        });
        setError(''); 
    };

    const validateForm = () => {
        if (!formData.username || !formData.email || !formData.password) {
            setError('Please fill in all required fields.');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);
        const currentDateTime = new Date().toISOString();

        const dataToSubmit = {
            ...formData,
            updatedAt: currentDateTime,
            yesterdayPrice: formData.yesterdayPrice || 0,
        };

        try {
            const token = localStorage.getItem('jwt');
            const url = `${process.env.REACT_APP_SETTING_URL}/${formData.username}`

            const response = await axios.put(url, dataToSubmit, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            console.log('Trader updated:', response.data);
            setModalTitle('Success');
            setModalMessage("You've successfully updated your settings!");
            setModalOpened(true);

            setCountdown(3);
            const timerId = setInterval(() => {
                setCountdown((prev) => {
                    if (prev <= 1) {
                        clearInterval(timerId);
                        goToHomePage();
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);

            
            setFormData({
                username: '',
                email: '',
                password: '',
                firstName: '',
                lastName: '',
                userId: '',
                traderNumber: '',
                usdtBalance: 0,
                idNumber: '',
                phoneNumber: '',
                yesterdayPrice: '',
            });
        } catch (error) {
            console.error('Error updating trader:', error);
        setError('Failed to update trader. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const goToHomePage = () => {
        navigate('/home');
    };

    const validatePhoneNumber = (phone) => {
        if (phone.length === 0) return true; 
        const re = /^\(?([0-9]{0,3})\)?[- ]?([0-9]{0,3})[- ]?([0-9]{0,4})$/; 
        return re.test(phone);
    };

    return (
        <MantineProvider>
            <div style={styles.container}>
                <Card shadow="lg" padding="xl" radius="md" withBorder style={styles.card}>
                    <Title order={2} align="center" color="white">Fill In the form to finish the KYC</Title>
                    {error && <Text color="red">{error}</Text>}

                    <form onSubmit={handleSubmit}>
                        <TextInput
                            label="Username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            style={styles.input}
                        />
                        <TextInput
                            label="Email"
                            name="email"
                            type="email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                            style={styles.input}
                        />
                        <TextInput
                            label="Password"
                            name="password"
                            type="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                            style={styles.input}
                        />
                        <TextInput
                            label="First Name"
                            name="firstName"
                            value={formData.firstName}
                            onChange={handleChange}
                            required
                            style={styles.input}
                        />
                        <TextInput
                            label="Last Name"
                            name="lastName"
                            value={formData.lastName}
                            onChange={handleChange}
                            required
                            style={styles.input}
                        />
                        <TextInput
                            label="USDT Balance"
                            name="usdtBalance"
                            type="number"
                            value={formData.usdtBalance}
                            onChange={handleChange}
                            required
                            style={styles.input}
                        />
                        <small style={{ color: 'gray' }}>Maximum balance allowed: 9,999,999 USDT</small>
                        <TextInput
                            label="ID Number"
                            name="idNumber"
                            value={formData.idNumber}
                            onChange={handleChange}
                            required
                            style={styles.input}
                        />
                        <TextInput
                            label="Phone Number"
                            name="phoneNumber"
                            value={formData.phoneNumber}
                            onChange={handleInputChange}
                            style={styles.input}
                        />

                        <Button type="submit" disabled={isSubmitting} fullWidth>
                          Update Trader
                        </Button> 
                    </form>
                    
                    {modalOpened && !error && (
                    <div style={{ marginTop: '20px' }}>
                      <h3>{modalTitle}</h3>
                      <p>{modalMessage}</p>
                      {countdown > 0 && (
                        <p>We will direct you to the dashboard in {countdown} seconds.</p>
                      )}
                      <Button onClick={() => setModalOpened(false)}>Close</Button>
                    </div>
                )}

                    <Button onClick={goToHomePage} variant="outline" fullWidth style={{ marginTop: '20px' }}>
                      Back to Home Page
                    </Button> 
                </Card>
            </div>
        </MantineProvider>
    );
};

const styles = {
    container: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh', 
        backgroundColor: 'black',
    },
    card: {
      backgroundColor: '#1c1c1c', 
      width: '400px', 
      maxWidth: '90%', 
    },
    input: {
      marginBottom: '15px',
    },
};

export default Settings;