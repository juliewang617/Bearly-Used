import React, { useState, useEffect } from "react";
import "../styles/SetupProfile.css"; // Can reuse the same styles
import { useUser } from "@clerk/clerk-react";

interface SetupProfileProps {
  setFirstLogin: React.Dispatch<React.SetStateAction<boolean>>;
}

/**
 * Renders a Set Up Profile Page. Only displayed when its the user's first time logging in.
 *
 * @returns {JSX.Element} A JSX element representing a Set Up Profile Page.
 */
const SetupProfile = ({ setFirstLogin }: SetupProfileProps) => {
  const [name, setName] = useState("");
  const [school, setSchool] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [error, setError] = useState<string | null>(null);

  const { user } = useUser();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!name || !school || !phoneNumber) {
      setError("All fields are required.");
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:3232/add-user?clerk_id=${user?.id}&email=${user?.emailAddresses}&name=${name}&phone_number=${phoneNumber}&school=${school}`
      );

      const data = await response.json();

      if (data.response_type === "success") {
        setFirstLogin(false);
      } else {
        console.log(data);
        setError("Error creating profile.");
      }
    } catch (err) {
      console.error("Error creating profile:", err);
      setError("An unexpected error occurred. Please try again.");
    }
  };

  return (
    <div
      className="setup-profile-container"
      aria-label="Setup profile container"
    >
      <h2 aria-label="Setup profile heading">Configure Your Information</h2>
      <form
        onSubmit={handleSubmit}
        className="setup-profile-form"
        aria-label="Setup profile form"
      >
        {error && (
          <p className="error-message" aria-label={`Error: ${error}`}>
            {error}
          </p>
        )}

        {/* Configuring Name */}
        <div className="form-group" aria-label="Name input field">
          <label htmlFor="name">Name</label>
          <input
            id="name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter your full name"
            aria-label="Enter your full name"
            required
          />
        </div>
        {/* Configuring Phone Number */}
        <div className="form-group" aria-label="Phone number input field">
          <label htmlFor="phoneNumber">Phone Number</label>
          <input
            id="phoneNumber"
            type="text"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            placeholder="123-456-7890"
            required
            aria-label="Enter your phone number in the format 123-456-7890"
          />
        </div>
        {/* Configuring School */}
        <div className="form-group" aria-label="School selection field">
          <label htmlFor="school">School:</label>
          <select
            id="school"
            value={school}
            onChange={(e) => setSchool(e.target.value)}
            aria-label="Select your school"
            required
          >
            <option value="" disabled>
              Select your school
            </option>
            <option value="Brown">Brown</option>
            <option value="RISD">RISD</option>
          </select>
        </div>
        <button
          type="submit"
          aria-label="Save and continue button"
          className="btn btn-primary"
        >
          Save and Continue
        </button>
      </form>
    </div>
  );
};

export default SetupProfile;
