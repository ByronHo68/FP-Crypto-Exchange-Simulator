import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button, TextInput, Paper, Title, Text, Loader } from "@mantine/core";
import "./Signup.css";

const SIGNUP_URL = process.env.REACT_APP_SIGNUP_URL;
const SIGNUP_ADMIN_URL = process.env.REACT_APP_SIGNUP_ADMIN_URL;

const Signup = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [secretCode, setSecretCode] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");

    const signupUrl = secretCode ? SIGNUP_ADMIN_URL : SIGNUP_URL;

    try {
      const response = await fetch(signupUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          displayName,
          email,
          password,
          secretCode,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Signup failed");
      }

      const data = await response.json();
      console.log("Signup successful:", data);
      navigate("/login");
    } catch (err) {
      console.error("Error during signup:", err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (setter) => (event) => {
    setter(event.currentTarget.value);
  };

  return (
    <div className="signup-container">
      <Paper className="signup-paper">
        <Title order={2} className="title">
          Sign Up
        </Title>
        {error && (
          <Text style={{ color: "black" }} align="center">
            {error}
          </Text>
        )}
        <form onSubmit={handleSubmit}>
          <TextInput
            value={displayName}
            onChange={handleChange(setDisplayName)}
            label="Display Name"
            placeholder="Enter your display name"
            required
            className="text-input"
          />
          <TextInput
            value={email}
            onChange={handleChange(setEmail)}
            label="Email"
            placeholder="Enter your email"
            type="email"
            required
            className="text-input"
          />
          <TextInput
            value={password}
            onChange={handleChange(setPassword)}
            label="Password"
            placeholder="Enter your password"
            type="password"
            required
            className="text-input"
          />
          <TextInput
            value={secretCode}
            onChange={handleChange(setSecretCode)}
            label="Secret Code (optional)"
            placeholder="Enter your secret code"
            className="text-input"
          />
          <Button type="submit" className="submit-button" loading={loading}>
            {loading ? <Loader color="white" size="xs" /> : "Submit"}
          </Button>
        </form>
        <Button
          type="button"
          onClick={() => navigate("/login")}
          className="signin-button"
        >
          Already have an account? Sign In
        </Button>
      </Paper>
    </div>
  );
};

export default Signup;
