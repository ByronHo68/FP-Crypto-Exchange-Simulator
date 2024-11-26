import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Button,
  TextInput,
  Paper,
  Title,
  Text,
  Loader,
  Modal,
} from "@mantine/core";
import axios from "axios";
import "./Login.css";

const Login = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [modalTitle, setModalTitle] = useState("");
  const [modalMessage, setModalMessage] = useState("");
  const [modalOpened, setModalOpened] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    if (!email || !password) {
      setError("Email and password are required.");
      setLoading(false);
      return;
    }

    if (!/\S+@\S+\.\S+/.test(email)) {
      setError("Invalid email format.");
      setLoading(false);
      return;
    }

    try {
      const apiKey = process.env.REACT_APP_FIREBASE_API_KEY;
      const baseUrl = process.env.REACT_APP_FIREBASE_SIGN_IN_URL;
      //const url = `${baseUrl}?key=${apiKey}`;

      const response = await axios.post(baseUrl, {
        email: email,
        password: password,
        returnSecureToken: true,
      });

      localStorage.setItem("jwt", response.data.idToken);
      localStorage.setItem("displayName", response.data.displayName || "");
      localStorage.setItem("uid", response.data.localId);

      setModalTitle("Success");
      setModalMessage("You've successfully logged in! Redirecting...");
      setModalOpened(true);

      // Reset form fields
      setEmail("");
      setPassword("");

      navigate("/home");
    } catch (error) {
      const errorMessage =
        error.response?.data?.error?.message ||
        "Login failed. Please try again.";
      setError(errorMessage);
      setModalTitle("Error");
      setModalMessage(errorMessage);
      setModalOpened(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <Paper className="login-paper">
        <Title order={2} className="title">
          Login
        </Title>
        {error && (
          <Text style={{ color: "black" }} align="center">
            {error}
          </Text>
        )}
        <form onSubmit={handleSubmit}>
          <TextInput
            label="Email"
            placeholder="Enter your Email"
            required
            value={email}
            onChange={(event) => setEmail(event.currentTarget.value)}
            className="text-input"
          />
          <TextInput
            label="Password"
            placeholder="Enter your password"
            type="password"
            required
            value={password}
            onChange={(event) => setPassword(event.currentTarget.value)}
            className="text-input"
          />
          <Button type="submit" className="submit-button" loading={loading}>
            {loading ? <Loader color="white" size="xs" /> : "Submit"}
          </Button>
          <Button
            type="button"
            onClick={() => navigate("/signup")}
            className="signup-button"
          >
            Sign Up
          </Button>
        </form>
      </Paper>

      <Modal
        opened={modalOpened}
        onClose={() => setModalOpened(false)}
        title={modalTitle}
      >
        <Text>{modalMessage}</Text>
      </Modal>
    </div>
  );
};

export default Login;
