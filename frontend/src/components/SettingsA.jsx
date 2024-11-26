import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Settings = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    userId: "",
    instructorNumber: "",
  });

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [modalOpened, setModalOpened] = useState(false);
  const [modalTitle, setModalTitle] = useState("");
  const [modalMessage, setModalMessage] = useState("");

  useEffect(() => {
    const jwt = localStorage.getItem("jwt");
    const displayName = localStorage.getItem("displayName") || "";
    const uid = localStorage.getItem("uid") || "";
    const email = jwt ? JSON.parse(atob(jwt.split(".")[1])).email : "";

    setFormData((prevData) => ({
      ...prevData,
      username: displayName,
      email: email,
      userId: uid,
    }));
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
    setError("");
  };

  const validateForm = () => {
    if (!formData.username || !formData.email || !formData.password) {
      setError("Please fill in all required fields.");
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);
    const currentDateTime = new Date().toISOString();

    const dataToSubmit = {
      ...formData,
      updatedAt: currentDateTime,
    };

    try {
      const token = localStorage.getItem("jwt");
      const url = `${process.env.REACT_APP_SETTING_TRADER_URL}/${formData.username}`;
      const response = await axios.put(url, dataToSubmit, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      console.log("Instructor updated:", response.data);
      setModalTitle("Success");
      setModalMessage("You've successfully updated your settings!");
      setModalOpened(true);

      setFormData({
        username: "",
        email: "",
        password: "",
        firstName: "",
        lastName: "",
        userId: "",
        instructorNumber: "",
      });
    } catch (error) {
      console.error("Error updating instructor:", error);
      setError("Failed to update instructor. Please try again.");
      setModalTitle("Error");
      setModalMessage("Failed to update instructor. Please try again.");
      setModalOpened(true);
    } finally {
      setIsSubmitting(false);
    }
  };

  const goToHomePage = () => {
    navigate("/home");
  };

  return (
    <div>
      <h2>Settings Page</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <form onSubmit={handleSubmit}>
        <div>
          <label>Username:</label>
          <input
            type="text"
            name="username"
            value={formData.username}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Email:</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Password:</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>First Name:</label>
          <input
            type="text"
            name="firstName"
            value={formData.firstName}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>Last Name:</label>
          <input
            type="text"
            name="lastName"
            value={formData.lastName}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>Instructor Number:</label>
          <input
            type="text"
            name="instructorNumber"
            value={formData.instructorNumber}
            onChange={handleChange}
          />
        </div>

        <button type="submit" disabled={isSubmitting}>
          Update Instructor
        </button>
      </form>

      {modalOpened && (
        <div style={{ marginTop: "20px" }}>
          <h3>{modalTitle}</h3>
          <p>{modalMessage}</p>
          <button
            onClick={() => {
              setModalOpened(false);
              setModalMessage("");
            }}
          >
            Close
          </button>
        </div>
      )}

      <button onClick={goToHomePage} style={{ marginTop: "20px" }}>
        Back to Home Page
      </button>
    </div>
  );
};

export default Settings;
