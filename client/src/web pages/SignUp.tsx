import "../styles/Signup.css";
import { SignInButton } from "@clerk/clerk-react";
import logo from "../assets/logo.png";

/**
 * Renders a Sign Up Page. Prompts the user to log in.
 *
 * Only users signing in with a Brown or RISD email can proceed forward to the website.
 *
 * @returns {JSX.Element} A JSX element representing a Sign Up Page.
 */
const SignUp = () => {
  return (
    <div className="sign-up-container" aria-label="Sign up container">
      <img
        src={logo}
        style={{ height: "150px" }}
        aria-label="Bearly Used logo"
      />
      <h2 aria-label="Bearly Used title">Bearly Used</h2>
      <p aria-label="Sign in instructions">
        Sign in with your Brown/RISD email to access Bearly Used!
      </p>
      <SignInButton>
        <button className="btn btn-primary" aria-label="Sign in button">
          Sign In
        </button>
      </SignInButton>
    </div>
  );
};

export default SignUp;
