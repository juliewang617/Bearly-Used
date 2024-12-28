import React from "react";
import { HashRouter as Router, Routes, Route } from "react-router-dom";
import HomePage from "./web pages/HomePage";
import UserProfile from "./web pages/UserProfile";
import SellerProfile from "./web pages/SellerProfile";
import "bootstrap/dist/css/bootstrap.min.css";
import ProductPage from "./web pages/ProductPage";
import "./styles/App.css";
import { useState, useEffect } from "react";
import {
  SignedIn,
  SignedOut,
  SignOutButton,
  useUser,
} from "@clerk/clerk-react";
import SetupProfile from "./web pages/SetupProfile";
import SignUp from "./web pages/SignUp";
import logo from "./assets/logo.png";

type userInfo = {
  id: number;
  clerk_id: string;
  name: string;
  email: string;
  phone_number: string;
  school: string;
  tags: string[];
};

/**
 * This is the highest level component which builds the application.
 * It manages user log ins and routing of the application's pages.
 *
 * @return JSX of the entire application
 */
const App: React.FC = () => {
  const { isSignedIn, user } = useUser();
  const [firstLogin, setFirstLogin] = useState(false);
  const [userInfo, setUserInfo] = useState<userInfo | null>(null);

  useEffect(() => {
    // checks if user has loggined in for the first time. if so, send them to
    // setupProfilepage
    const checkIfFirstLogin = async () => {
      if (isSignedIn && user) {
        try {
          const response = await fetch(
            `http://localhost:3232/get-user?clerk_id=${user.id}`
          );
          const data = await response.json();
          if (data.response_type === "success") {
            setUserInfo(data.user_data);
            setFirstLogin(false);
          } else {
            setFirstLogin(true);
          }
        } catch (error) {
          console.error("error checking if user details are set up", error);
        }
      }
    };
    checkIfFirstLogin();
  }, [isSignedIn, user]);

  // const handleUserClick = (id: number) => {
  //   navigate(`/user/${id}?${searchParams.toString()}`);
  // };

  return (
    <div aria-label="Application Container">
      <SignedOut>
        <SignUp aria-label="Sign-Up Page" />
      </SignedOut>
      <SignedIn>
        <Router>
          {firstLogin ? (
            <SetupProfile
              setFirstLogin={setFirstLogin}
              aria-label="Setup Profile Page"
            />
          ) : (
            //<SetupProfile setFirstLogin={setFirstLogin} />
            <div>
              <nav
                className="navbar navbar-expand-lg navbar-custom"
                aria-label="Main Navigation"
              >
                <div
                  className="container-fluid navbar-container"
                  aria-label="Navigation Container"
                >
                  <a
                    className="navbar-brand"
                    href="/"
                    aria-label="Bearly Used Home Page Link"
                  >
                    <img
                      src={logo}
                      style={{ height: "50px", marginRight: "10px" }}
                      alt="Bearly Used Logo"
                      aria-label="Application Logo"
                    />
                    Bearly Used
                  </a>
                  <div
                    className="user-profile-section"
                    aria-label="User Profile Section"
                  >
                    <a
                      href="/#/user"
                      className="user-name"
                      aria-label="User Profile Page Link"
                    >
                      {userInfo?.name || "username"}
                    </a>
                    <a href="/#/user" aria-label="User Profile Page Link">
                      <img
                        alt="Profile"
                        className="profile-picture"
                        src={user?.imageUrl}
                        aria-label="User Profile Picture"
                      />
                    </a>
                    <SignOutButton>
                      <button
                        className="btn-signout"
                        aria-label="Sign Out Button"
                      >
                        Sign out
                      </button>
                    </SignOutButton>
                  </div>
                </div>
              </nav>

              <Routes>
                <Route path="/" element={<HomePage aria-label="Home Page" />} />
                <Route
                  path="/user"
                  element={<UserProfile aria-label="User Profile Page" />}
                />
                <Route
                  path="/product/:id"
                  element={<ProductPage aria-label="Product Page" />}
                />
                <Route
                  path="/seller/:sellerId"
                  element={<SellerProfile aria-label="Seller Profile Page" />}
                />
              </Routes>
            </div>
          )}
        </Router>
      </SignedIn>
    </div>
  );
};

export default App;
