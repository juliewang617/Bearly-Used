import React, { useState, useEffect } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import "../styles/UserProfile.css";
import ListItemPopup from "./ListItemPopup";
import { Modal } from "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { getUserListings, getUserProfile } from "../api";
import { useUser } from "@clerk/clerk-react";
import EditProfilePopup from "./EditProfilePopup";
import { supabase } from "../utils/supabaseClient";

type Listing = {
  id: number;
  title: string;
  price: number;
  description: string;
  category: string;
  condition: string;
  image_url: string;
  tags: string[];
  available: boolean;
};

type UserProfile = {
  id: number;
  clerk_id: string;
  name: string;
  email: string;
  phone_number: string;
  school: string;
  tags: string[];
};

/**
 * Renders a User Profile Page. Displays the User's information listings.
 *
 * @returns {JSX.Element} A JSX element representing a User Profile Page.
 */
const UserProfile: React.FC = () => {
  const { user } = useUser();
  const [listings, setListings] = useState<Listing[]>([]);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [listingsPage, setListingsPage] = useState(0);
  const [boughtPage, setBoughtPage] = useState(0);
  const ITEMS_PER_PAGE = 4;
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [product, setProduct] = useState<Listing | null>(null);

  const { id } = useParams();
  console.log("User ID from useParams:", id);

  useEffect(() => {
    const userId = user?.id || "";
    fetchUserData(userId);
    fetchUserListings(userId);
  }, []);

  const fetchUserData = async (userId: string) => {
    try {
      const userData = await getUserProfile(userId);
      console.log("User data received:", userData);
      setUserProfile(userData.user_data);
    } catch (error) {
      console.error("Failed to fetch user data:", error);
    }
  };

  const fetchUserListings = async (userId: string) => {
    try {
      const listingsData = await getUserListings(userId);
      console.log("listing data", listingsData);
      setListings(listingsData.listings ? listingsData.listings : []);
    } catch (error) {
      console.error("Failed to fetch user listings:", error);
    }
  };

  const handleDeleteListing = async (listingId: number) => {
    try {
      // delete the image from storage first
      if (product?.image_url) {
        const { error } = await supabase.storage
          .from("images")
          .remove([product.image_url]);
        if (error) {
          console.error("Error removing image from storage", error.message);
          return;
        }
      }
      const response = await fetch(
        `http://localhost:3232/delete-listing?listing_id=${listingId}`
      );
      const data = await response.json();
      if (data.response_type === "success") {
        alert("Listing successfully deleted.");
        fetchUserListings(userProfile?.clerk_id || "123");
      } else {
        alert("Failed to delete the listing.");
      }
    } catch (error) {
      console.error("Error deleting listing:", error);
      alert("An error occurred while trying to delete the listing.");
    }
  };

  // paginate for arrow press on listings/items bought
  const paginate = (items: Listing[], page: number): Listing[] =>
    items.slice(page * ITEMS_PER_PAGE, (page + 1) * ITEMS_PER_PAGE);

  const handleNextPage = (
    page: number,
    setPage: React.Dispatch<React.SetStateAction<number>>,
    items: Listing[]
  ) => {
    if ((page + 1) * ITEMS_PER_PAGE < items.length) setPage(page + 1);
  };

  const handlePrevPage = (
    page: number,
    setPage: React.Dispatch<React.SetStateAction<number>>
  ) => {
    if (page > 0) setPage(page - 1);
  };

  const visibleListings = paginate(listings, listingsPage);
  // uses listings for now, need separate handler later
  const visibleBought = paginate(listings, boughtPage);

  const closeModal = () => {
    const modalElement = document.getElementById("addListingModal");
    if (modalElement) {
      const modalInstance = Modal.getInstance(modalElement);
      if (modalInstance) {
        modalInstance.dispose();
      }

      document.body.classList.remove("modal-open");
      document.body.style.removeProperty("padding-right");
      document.body.style.overflow = "initial";
      const backdrops = document.querySelectorAll(".modal-backdrop");
      backdrops.forEach((backdrop) => backdrop.remove());
    }
  };

  const [editingListing, setEditingListing] = useState<Listing | null>(null);
  const [editingProfile, setEditingProfile] = useState<UserProfile | null>(
    null
  );

  const handleEditProfile = () => {
    setEditingProfile(userProfile);
  };

  const handleEditListing = (listing: Listing) => {
    setEditingListing(listing);
  };

  const handleUpdateUserProfile = async (formData: UserProfile) => {
    try {
      console.log("formData", formData);
      const response = await fetch(
        `http://localhost:3232/update-user?clerk_id=${user?.id}&name=${formData.name}&phone_number=${formData.phone_number}&school=${formData.school}`
      );
      const data = await response.json();
      console.log("bbbb", data);
      if (data.response_type === "success") {
        alert("Profile succesfully updated. Refresh to see changes!");
        navigate("/user");
      } else {
        alert("Error: " + data.error);
      }
    } catch (error) {
      console.error("Error updating profile:", error);
      alert("Error: " + error);
    }
  };

  const handleMarkAsSold = async (
    listing: { id: any } | undefined,
    available: boolean
  ) => {
    try {
      const response = await fetch(
        `http://localhost:3232/update-listing?listing_id=${listing?.id}&available=${available}`
      );
      const data = await response.json();
      console.log("bbbb", data);
      if (data.response_type === "success" && available === false) {
        alert("Listing successfully marked as unavailable.");
        window.location.reload();
      } else if (data.response_type === "success" && available === true) {
        alert("Listing successfully marked as available.");
        window.location.reload();
      } else {
        alert("Failed to mark as sold.");
      }
    } catch (error) {
      console.error("Error marking as sold:", error);
      alert("An error occurred while trying to mark the listing as sold");
    }
  };

  // Initialize ListItemPopup modal for creating Listings
  useEffect(() => {
    const modalElement = document.getElementById("addListingModal");
    if (modalElement) {
      new Modal(modalElement);
    }
  }, []);

  // Initialize ListItemPopup modal for editing Listings
  useEffect(() => {
    const modalElement = document.getElementById("editListingModal");
    if (modalElement) {
      new Modal(modalElement);
    }
  }, []);

  //trying to set this up to redirect not to /user but to /user/userid.
  //cuz we need the id for the backend handlers to grab user specific data
  //for some reason its only going to /user.
  const navigate = useNavigate();
  const handleUserClick = (id: number) => {
    navigate(`/user/${id}`);
  };

  return (
    <div className="user-profile-container" aria-label="User profile page">
      <div className="header" aria-label="Navigation header">
        {" "}
        <Link to="/" className="back-link" aria-label="Go back to homepage">
          <svg
            className="back-icon"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </Link>
        <button
          className="create-listing"
          data-bs-toggle="modal"
          data-bs-target="#addListingModal"
          aria-label="Create new listing"
        >
          Create Listing
        </button>
      </div>

      <div className="profile-container" aria-label="User profile details">
        <div className="profile">
          <img
            className="user-page-profile-picture"
            src={user?.imageUrl}
            alt="User profile"
          ></img>
          <div className="profile-info">
            <h2 className="name" aria-label={`User name: ${userProfile?.name}`}>
              {userProfile?.name || "Loading..."}
            </h2>
            <p className="school" aria-label={`School: ${userProfile?.school}`}>
              School: {userProfile?.school || "Loading..."}
            </p>
            <p className="email" aria-label={`Email: ${userProfile?.email}`}>
              Email: {userProfile?.email || "Loading..."}
            </p>
            <p
              className="phone"
              aria-label={`Phone number: ${userProfile?.phone_number}`}
            >
              Phone Number: {userProfile?.phone_number || "Loading..."}
            </p>

            {/* <div className="tags">
              Interests:
              {userProfile?.tags?.map((tag: string) => (
                <span key={tag} className="tag">
                  {tag}
                </span>
              ))}
            </div> */}
          </div>
        </div>
        <button
          className="edit-profile"
          aria-label="Edit profile"
          onClick={handleEditProfile}
        >
          Edit Profile
        </button>

        {/* EditProfilePopup to Edit Profile */}
        {editingProfile && (
          <div
            className="modal fade show"
            aria-label="Edit Profile Modal"
            style={{ display: "block", background: "rgba(0,0,0,0.5)" }}
          >
            <div
              className="modal-dialog modal-lg modal-dialog-centered"
              aria-labelledby="editProfileTitle"
            >
              <div className="modal-content">
                <button
                  type="button"
                  className="btn-close"
                  style={{
                    fontSize: "0.75rem",
                    margin: "0.5rem",
                    padding: "0.25rem",
                  }}
                  onClick={() => {
                    setEditingProfile(null);
                    document.body.style.overflow = "auto";
                  }}
                  aria-label="Close Edit Profile Modal"
                ></button>

                <div className="modal-body" aria-label="Edit Profile Form">
                  {userProfile ? (
                    <EditProfilePopup
                      initialData={userProfile}
                      onSubmit={(formData) => {
                        setEditingProfile(null);
                        handleUpdateUserProfile(formData);
                        fetchUserData(userProfile?.clerk_id || "123");
                        document.body.style.overflow = "auto";
                      }}
                    />
                  ) : null}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* ListItemPopup to Edit Listing */}
        {editingListing && (
          <div
            className="modal fade show"
            aria-label="Edit Listing Modal"
            style={{ display: "block", background: "rgba(0,0,0,0.5)" }}
          >
            <div
              className="modal-dialog modal-lg modal-dialog-centered"
              aria-labelledby="editListingTitle"
            >
              <div className="modal-content">
                <button
                  type="button"
                  className="btn-close"
                  style={{
                    fontSize: "0.75rem",
                    margin: "0.5rem",
                    padding: "0.25rem",
                  }}
                  onClick={() => {
                    setEditingListing(null);
                    document.body.style.overflow = "auto";
                  }}
                  aria-label="Close Edit Listing Modal"
                ></button>

                <div className="modal-body" aria-label="Edit Listing Form">
                  <ListItemPopup
                    isEditing={true}
                    initialData={{
                      sellerId: userProfile?.id || 1,
                      title: editingListing.title,
                      available: editingListing.available,
                      description: editingListing.description,
                      price: editingListing.price,
                      category: editingListing.category,
                      condition: editingListing.condition,
                      imageUrl: editingListing.image_url,
                      tags: editingListing.tags,
                      images: [],
                    }}
                    editId={editingListing.id}
                    onSubmit={() => {
                      setEditingListing(null);
                      fetchUserListings(userProfile?.clerk_id || "123");
                      document.body.style.overflow = "auto";
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      <h2 aria-label="My listings section">My Listings</h2>
      <div className="listings-navigation" aria-label="Listings navigation">
        <button
          className="arrow-btn"
          onClick={() => handlePrevPage(listingsPage, setListingsPage)}
          disabled={listingsPage === 0}
          aria-label="Previous page of listings"
        >
          &#8592;
        </button>
        <div className="listings" aria-label="List of user listings">
          {visibleListings.map((listing: Listing) => (
            <div
              key={listing.id}
              className="listing"
              aria-label={`Listing for ${listing.title}`}
            >
              <img
                className="listing-image"
                src={listing.image_url}
                alt={`${listing.title} image`}
              />
              <p
                className="listing-price"
                aria-label={`Price: $${listing.price}`}
              >
                ${listing.price.toFixed(2)}
              </p>
              <p
                className="listing-name"
                aria-label={`Listing title: ${listing.title}`}
              >
                {listing.title}
              </p>
              {!listing.available && (
                <div className="sold-badge" aria-label="Sold badge">
                  SOLD
                </div>
              )}
              <div className="listing-actions" aria-label="Listing actions">
                <button
                  className="btn btn-sm btn-outline-primary"
                  onClick={() => handleEditListing(listing)}
                  title="Edit"
                  aria-label="Edit listing"
                >
                  <i className="bi bi-pencil"></i>
                </button>
                <button
                  className="btn btn-sm btn-outline-success"
                  onClick={() => {
                    handleMarkAsSold(listing, !listing.available);
                  }}
                  aria-label={
                    listing.available
                      ? "Mark listing as sold"
                      : "Unmark listing as sold"
                  }
                >
                  {!listing.available ? "Unmark as Sold" : "Mark as Sold"}
                </button>

                <button
                  className="btn btn-sm btn-outline-danger"
                  title="Delete"
                  onClick={() => {
                    setProduct(listing);
                    setShowDeleteConfirm(true);
                  }}
                >
                  <i className="bi bi-trash"></i>
                </button>
              </div>
            </div>
          ))}
        </div>
        <button
          className="arrow-btn"
          onClick={() =>
            handleNextPage(listingsPage, setListingsPage, listings)
          }
          disabled={(listingsPage + 1) * ITEMS_PER_PAGE >= listings.length}
          aria-label="Next page of listings"
        >
          &#8594;
        </button>
      </div>

      {/* currently uses listings */}
      {/* I'm hiding this since i don't think we're doing previously bought? - julie */}

      {/* <h2>Previously Bought</h2>
      <div className="listings-navigation">
        <button
          className="arrow-btn"
          onClick={() => handlePrevPage(boughtPage, setBoughtPage)}
          disabled={boughtPage === 0}
        >
          &#8592;
        </button>
        <div className="listings">
          {visibleBought.map((listing: Listing) => (
            <div key={listing.id + 12} className="listing">
              <img className="listing-image" src={listing.image_url} />
              <p className="listing-price">${listing.price}</p>
              <p className="listing-name">{listing.title}</p>
            </div>
          ))}
        </div>
        <button
          className="arrow-btn"
          onClick={() => handleNextPage(boughtPage, setBoughtPage, listings)}
          disabled={(boughtPage + 1) * ITEMS_PER_PAGE >= listings.length}
        >
          &#8594;
        </button>
      </div> */}

      <div
        className="modal fade"
        id="addListingModal"
        tabIndex={-1}
        aria-labelledby="addListingModalLabel"
        aria-hidden="true"
      >
        <div className="modal-dialog modal-lg modal-dialog-centered">
          <div className="modal-content">
            <button
              type="button"
              className="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
              style={{
                fontSize: "0.75rem",
                margin: "0.5rem",
                padding: "0.25rem",
              }}
            ></button>

            <div className="modal-body">
              <ListItemPopup
                onSubmit={() => {
                  closeModal();
                  fetchUserListings(userProfile?.clerk_id || "123");
                  document.body.style.overflow = "auto";
                }}
              />
            </div>
          </div>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteConfirm && product && (
        <div
          className="modal fade show"
          style={{ display: "block", background: "rgba(0,0,0,0.5)" }}
          tabIndex={-1}
        >
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Delete Listing</h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => setShowDeleteConfirm(false)}
                ></button>
              </div>
              <div className="modal-body">
                <p>
                  Are you sure you want to delete this listing? This action
                  cannot be undone.
                </p>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-cancel"
                  onClick={() => setShowDeleteConfirm(false)}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={() => {
                    handleDeleteListing(product.id);
                    setShowDeleteConfirm(false);
                  }}
                >
                  <i id="confirm-delete-listing" className="bi bi-trash"></i>{" "}
                  Delete listing
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfile;
