import React, { useState, useEffect } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import mockProducts from "../data/product";
import ListItemPopup from "./ListItemPopup";
import "../styles/ProductPage.css";
import { Modal } from "bootstrap";
import { getUserListings } from "../api";
import { useUser } from "@clerk/clerk-react";
import { supabase } from "../utils/supabaseClient";

type UserProfile = {
  id: number;
  clerk_id: string;
  name: string;
  email: string;
  phone_number: string;
  school: string;
  tags: string[];
};

type Listing = {
  id: number;
  seller_id: string;
  title: string;
  price: number;
  description: string;
  category: string;
  condition: string;
  image_url: string;
  tags: string[];
  available: boolean;
};

interface Seller {
  clerk_id: string;
  id: number;
  name: string;
  email: string;
  phone_number: string;
  school: string;
}

/**
 * Renders the Product Page. Each Product has a unique Product page.
 *
 * @returns {JSX.Element} A JSX element representing a Product Page.
 */
const ProductPage: React.FC = () => {
  const { user } = useUser(); // retrieves the current user
  const { id } = useParams(); // gets the listing ID from the URL
  const navigate = useNavigate(); // handles navigation between pages
  // USED FOR MOCK DATA
  // const product = mockProducts.mockProducts.find((p) => p.id === Number(id));
  // const [mainImage, setMainImage] = useState(product?.images[0]);

  const [product, setProduct] = useState<Listing | null>(null); // stores the product details
  const [seller, setSeller] = useState<Seller | null>(null); // stores the seller's information
  const [mainImage, setMainImage] = useState<string>(""); // stores the main image of the product
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false); // controls the visibility of the delete confirmation modal

  const handleViewProfile = (sellerId: string) => {
    navigate(`/seller/${sellerId}`);
  };

  const handleBack = () => {
    navigate(-1); // Goes back to previous page
  };

  const copyEmail = () => {
    if (seller?.email) {
      navigator.clipboard.writeText(seller.email).then(() => {
        alert("Email copied to clipboard!"); // Could be replaced with a nicer toast notification
      });
    }
  };

  const copyEmailTemplate = () => {
    const template = `Hi,
  
  I'm interested in buying your item: ${product?.title} for $${product?.price}.
  
  Best regards,
  [Your Name]`;

    navigator.clipboard.writeText(template).then(() => {
      // Show success message
      alert("Email template copied to clipboard!");
    });
  };

  // Fetches the product data based on its id
  useEffect(() => {
    console.log("Listing ID:", id);
    if (!id) return;
    const fetchProduct = async () => {
      try {
        const response = await fetch(
          `http://localhost:3232/get-listing-by-id?listing_id=${id}`
        );
        const data = await response.json();
        console.log("Fetched listing data:", data);

        if (data.response_type === "success") {
          const fetchedProduct = data.listing;
          setProduct({ ...fetchedProduct });
          setMainImage(fetchedProduct.image_url);
        } else {
          console.error("Error fetching product data");
        }
      } catch (err) {
        console.error("Error fetching product data:", err);
      }
    };
    fetchProduct();
  }, [id]);

  // Fetches the seller
  useEffect(() => {
    const fetchSeller = async () => {
      try {
        const response = await fetch(
          `http://localhost:3232/get-user?clerk_id=${product?.seller_id}`
        );
        const data = await response.json();
        console.log("Fetched user:", data);
        setSeller({ ...data.user_data });
        console.log("new seller", seller);
        if (data.response_type === "success") {
        } else {
          console.error("Error fetching seller data");
        }
      } catch (err) {
        console.error("Error fetching seller:", err);
      }
    };
    if (product) {
      fetchSeller();
    }
  }, [product]);

  // delete listing
  const handleDeleteListing = async () => {
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
        `http://localhost:3232/delete-listing?listing_id=${id}`
      );
      const data = await response.json();
      if (data.response_type === "success") {
        alert("Listing successfully deleted.");
        navigate("/");
      } else {
        alert("Failed to delete the listing.");
      }
    } catch (error) {
      console.error("Error deleting listing:", error);
      alert("An error occurred while trying to delete the listing.");
    }
  };

  const [editingListing, setEditingListing] = useState<Listing | null>(null);

  const handleEditListing = (listing: Listing | null) => {
    const initialData = {
      title: listing?.title,
      available: listing?.available,
      description: listing?.description,
      price: listing?.price,
      category: listing?.category,
      condition: listing?.condition,
      imageUrl: listing?.image_url,
      tags: listing?.tags,
      images: [],
    };
    setEditingListing(listing);
  };

  const handleMarkAsSold = async () => {
    try {
      const response = await fetch(
        `http://localhost:3232/update-listing?listing_id=${id}&available=false`
      );
      const data = await response.json();
      console.log("bbbb", data);
      if (data.response_type === "success") {
        alert("Listing successfully marked as unavailable.");
        navigate("/");
      } else {
        alert("Failed to mark as sold.");
      }
    } catch (error) {
      console.error("Error marking as sold:", error);
      alert("An error occurred while trying to mark the listing as sold");
    }
  };

  // initializes the modal for editing the listing
  useEffect(() => {
    const modalElement = document.getElementById("editListingModal");
    if (modalElement) {
      new Modal(modalElement);
    }
  }, []);

  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);

  return (
    <div className="product-page">
      <div className="header">
        <button
          onClick={handleBack}
          className="back-link"
          aria-label="Go back to the previous page"
        >
          <svg
            className="back-icon"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
            aria-hidden="true"
          >
            <path d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </button>
      </div>

      <div className="product-container">
        <div className="row">
          {/* Product Images */}
          <div className="col-md-6">
            <div
              className="main-image-container"
              aria-label="Main Product Image"
            >
              <img
                src={mainImage}
                alt={product?.title}
                className="product-image"
              />
            </div>
          </div>

          {/* Product Info */}
          <div
            className="col-md-6 product-info"
            aria-label="Product Information"
          >
            <h1 className="product-title" aria-label="Product Title">
              {product?.title}{" "}
            </h1>
            <div className="product-price" aria-label="Product Price">
              ${product?.price}
            </div>
            <div className="tag-container" aria-label="Product Tags">
              {product?.tags.map((tag, index) => (
                <span key="tag" className="badge bg-secondary me-2">
                  {tag}
                </span>
              ))}
            </div>
            <p className="product-description" aria-label="Product Description">
              {product?.description}
            </p>

            {/* Seller Information Section */}
            <div
              className="seller-info-section"
              aria-label="Seller Information Section"
            >
              <h3>Seller Information</h3>
              <div className="seller-details" aria-label="Seller Details">
                <div
                  className="seller-detail"
                  aria-label={`Seller Name: ${seller?.name || "Anonymous"}`}
                >
                  <i className="bi bi-person"></i>
                  <span>{seller?.name || "Anonymous"}</span>
                </div>
                <div
                  className="seller-detail"
                  aria-label={`Seller School: ${
                    seller?.school || "Unknown School"
                  }`}
                >
                  <i className="bi bi-building"></i>
                  <span>{seller?.school || "Unknown School"}</span>
                </div>
                <div
                  className="seller-detail"
                  aria-label={`Seller Email: ${
                    seller?.email || "No email provided"
                  }`}
                >
                  <i className="bi bi-envelope"></i>
                  <span>{seller?.email || "No email provided"}</span>
                  <button
                    className="copy-email-btn"
                    onClick={copyEmail}
                    title="Copy email address"
                    aria-label="Copy email address to clipboard"
                  >
                    <i className="bi bi-clipboard" aria-hidden="true"></i>
                  </button>
                </div>
                <button
                  className="view-profile-btn"
                  onClick={() =>
                    seller?.clerk_id && handleViewProfile(seller.clerk_id)
                  }
                >
                  View Full Profile
                </button>
              </div>
            </div>

            {seller?.clerk_id === user?.id ? (
              <div className="action-buttons" aria-label="Seller Actions">
                <button
                  className="btn btn-edit"
                  onClick={() => handleEditListing(product)}
                  aria-label="Edit Listing"
                >
                  <i className="bi bi-pencil"></i> Edit
                </button>
                <button
                  className="btn btn-sold"
                  onClick={() => handleMarkAsSold()}
                  aria-label="Mark Listing as Sold"
                >
                  <i className="bi bi-check-circle"></i> Mark as sold
                </button>

                <button
                  className="btn btn-delete"
                  onClick={() => setShowDeleteConfirm(true)}
                  aria-label="Delete Listing"
                >
                  <i className="bi bi-trash"></i> Delete listing
                </button>

                {/* Delete Confirmation Modal */}
                <div
                  className={`modal fade ${showDeleteConfirm ? "show" : ""}`}
                  style={{ display: showDeleteConfirm ? "block" : "none" }}
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
                          aria-label="Delete Listing Confirmation"
                        ></button>
                      </div>
                      <div
                        className="modal-body"
                        aria-label="Are you sure you want to delete the listing?"
                      >
                        <p>
                          Are you sure you want to delete this listing? This
                          action cannot be undone.
                        </p>
                      </div>
                      <div className="modal-footer">
                        <button
                          type="button"
                          className="btn btn-cancel"
                          onClick={() => setShowDeleteConfirm(false)}
                          aria-label="Cancel"
                        >
                          Cancel
                        </button>
                        <button
                          type="button"
                          id="confirm-delete-listing"
                          className="btn btn-delete"
                          aria-label="Yes, Delete Listing"
                          onClick={() => {
                            handleDeleteListing();
                            setShowDeleteConfirm(false);
                          }}
                        >
                          Delete Listing
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Backdrop */}
                {showDeleteConfirm && (
                  <div
                    className="modal-backdrop fade show"
                    onClick={() => setShowDeleteConfirm(false)}
                  ></div>
                )}
              </div>
            ) : (
              <div className="action-buttons">
                <button className="btn btn-primary" onClick={copyEmailTemplate}>
                  <i className="bi bi-clipboard"></i> Copy Email Template
                </button>
              </div>
            )}
          </div>

          {/* ListItemPopup to Edit Listing */}
          {editingListing && (
            <div
              className="modal fade show"
              aria-label="Edit Listing Popup"

              style={{ display: "block", background: "rgba(0,0,0,0.5)" }}
            >
              <div className="modal-dialog modal-lg modal-dialog-centered">
                <div className="modal-content">
                  <button
                    type="button"
                    className="btn-close"
                    aria-label="Close popup"
                    style={{
                      fontSize: "0.75rem",
                      margin: "0.5rem",
                      padding: "0.25rem",
                    }}
                    onClick={() => {
                      setEditingListing(null);
                      document.body.style.overflow = "auto";
                    }}
                  ></button>
                  <div className="modal-body">
                    <ListItemPopup
                      isEditing={true}
                      initialData={{
                        sellerId: userProfile?.id || 1,
                        title: product?.title || "Product title",
                        available: product?.available || true,
                        description: product?.description || "desciption",
                        price: product?.price || 0,
                        category: product?.category || "category",
                        condition: product?.condition || "condition",
                        imageUrl: product?.image_url || "",
                        tags: product?.tags || [],
                        images: [],
                      }}
                      editId={product?.id}
                      onSubmit={() => {
                        setEditingListing(null);
                        getUserListings(userProfile?.clerk_id || "1");
                        document.body.style.overflow = "auto";
                      }}
                    />
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductPage;
