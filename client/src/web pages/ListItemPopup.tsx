import React, { useState, useEffect } from "react";
import "../styles/ListItemPopup.css";
import { Modal } from "bootstrap";
import { supabase } from "../utils/supabaseClient";
import { useUser } from "@clerk/clerk-react";
import { useNavigate } from "react-router-dom";

interface ListingForm {
  sellerId: number;
  title: string;
  available: boolean;
  description: string;
  price: number;
  category: string;
  condition: string;
  imageUrl: string;
  tags: string[];
  images: File[];
}

// defines the props that the ListItemPopup component can accept
interface ListItemPopupProps {
  onSubmit?: () => void;
  isEditing?: boolean;
  initialData?: ListingForm;
  editId?: number;
}

/**
 * Renders a ListItemPopup modal to allow the user to create a Listing or edit a preexisting Listing.
 *
 * @returns {JSX.Element} A JSX element representing a ListItemPopup modal.
 */
const ListItemPopup: React.FC<ListItemPopupProps> = ({
  onSubmit,
  isEditing = false,
  initialData,
  editId,
}) => {
  const { user } = useUser();
  const [formData, setFormData] = useState<ListingForm>(
    initialData || {
      sellerId: 0,
      title: "",
      available: true,
      description: "",
      price: 0,
      category: "",
      condition: "",
      imageUrl: "",
      tags: [],
      images: [],
    }
  );

  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [tagInput, setTagInput] = useState("");
  const [priceInput, setPriceInput] = useState("");
  const navigate = useNavigate();

  const categories = [
    "Electronics",
    "Furniture",
    "Appliances",
    "Clothing & Accessories",
    "Books",
    "Decor",
    "Tickets & Event Passes",
    "Other",
  ];
  const conditions = ["New", "Like New", "Good", "Fair", "Poor"];

  // populate form fields when editing an existing listing
  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
      setPriceInput(initialData.price.toString());
    }
  }, [initialData]);

  // generate image previews for uploaded images
  useEffect(() => {
    const objectUrls = formData.images.map((image) =>
      URL.createObjectURL(image)
    );
    console.log("objectUrls", objectUrls);

    if (formData.images.length > 0) {
      setFormData((prev) => ({
        ...prev,
        imageUrl: objectUrls[0],
      }));
    }
    return () => {
      objectUrls.forEach(URL.revokeObjectURL);
    };
  }, [formData.images]);

  // handles input changes for all form fields
  const handleInputChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ) => {
    const value =
      e.target.type === "checkbox"
        ? (e.target as HTMLInputElement).checked
        : e.target.type === "number"
        ? parseFloat(e.target.value)
        : e.target.value;

    setFormData({
      ...formData,
      [e.target.name]: value,
    });
  };

  // adds a new tag when the user presses "Enter"
  const handleTagKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && tagInput.trim()) {
      e.preventDefault();
      if (!formData.tags.includes(tagInput.trim())) {
        setFormData({
          ...formData,
          tags: [...formData.tags, tagInput.trim()],
        });
      }
      setTagInput("");
    }
  };

  // removes a tag from the list
  const removeTag = (tagToRemove: string) => {
    setFormData({
      ...formData,
      tags: formData.tags.filter((tag) => tag !== tagToRemove),
    });
  };

  // handles image uploads and appends them to the form data
  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setFormData({
        ...formData,
        images: [...formData.images, ...Array.from(e.target.files)],
      });
    }
  };

  const removeImage = (index: number) => {
    // Revoke the specific object URL before removing the image
    if (imagePreviews[index]) {
      URL.revokeObjectURL(imagePreviews[index]);
    }
    // Remove image from both formData and previews
    setFormData({
      ...formData,
      images: formData.images.filter((_, i) => i !== index),
    });
  };

  // handles form submission, uploading images and sending data to the backend
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // upload image to supabase storage and get the url
      const uploadedImageUrls = await Promise.all(
        formData.images.map(async (image: File) => {
          const fileName = `${Date.now()}-${image.name}`;
          const { data, error } = await supabase.storage
            .from("images")
            .upload(fileName, image);
          if (error) {
            console.error(
              `Error uploading image ${image.name}:`,
              error.message
            );
          }

          const res = supabase.storage.from("images").getPublicUrl(fileName);

          return res.data.publicUrl;
        })
      );

      const baseUrl = isEditing
        ? `http://localhost:3232/update-listing`
        : "http://localhost:3232/add-listing";

      const backendData = {
        seller_id: user?.id,
        title: formData.title,
        available: formData.available,
        description: formData.description,
        price: formData.price,
        category: formData.category,
        condition: formData.condition,
        imageUrl:
          uploadedImageUrls[0] != null
            ? uploadedImageUrls[0]
            : formData.imageUrl,
        tags: formData.tags,
      };
      console.log("Ready for backend:", backendData);

      // Add listing_id to query params when editing
      const queryParams = new URLSearchParams({
        seller_id: user?.id || "",
        title: backendData.title,
        available: backendData.available.toString(),
        description: backendData.description,
        price: backendData.price.toString(),
        category: backendData.category,
        condition: backendData.condition,
        image_url: backendData.imageUrl,
        tags: backendData.tags.join(","),
      });

      // Add listing_id only when editing
      if (isEditing && editId) {
        queryParams.append("listing_id", editId.toString());
      }

      const response = await fetch(`${baseUrl}?${queryParams.toString()}`);

      if (!response.ok) {
        throw new Error(
          isEditing ? "Failed to update listing" : "Failed to add listing"
        );
      }

      const result = await response.json();
      console.log(
        isEditing
          ? "Listing updated successfully:"
          : "Listing added successfully:",
        result
      );

      const modalElement = document.getElementById("addListingModal");
      if (modalElement) {
        const modalInstance = Modal.getInstance(modalElement);
        if (modalInstance) {
          modalInstance.hide();
        }
      }

      // reset form data and notify the parent component
      setFormData({
        sellerId: 0,
        title: "",
        available: true,
        description: "",
        price: 0,
        category: "",
        condition: "",
        imageUrl: "",
        tags: [],
        images: [],
      });

      setPriceInput("");
      setTagInput("");
      setImagePreviews([]);

      onSubmit?.();
      navigate(-1);
    } catch (error) {
      console.error(
        isEditing ? "Error updating listing:" : "Error adding listing:",
        error
      );
    }
  };

  return (
    <div>
      <h2 className="text-center mb-4">
        {isEditing ? "Edit Listing" : "Create Listing"}
      </h2>

      {/* form for creating or editing a listing */}
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label">Title</label>
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={handleInputChange}
            className="form-control"
            required
            aria-label="Enter the title of the listing"
          />
        </div>

        {/* description input */}
        <div className="mb-3">
          <label className="form-label">Description</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            className="form-control"
            rows={4}
            required
            aria-label="Enter a description for the listing"
          />
        </div>

        {/* price and category inputs */}
        <div className="row mb-3">
          <div className="col-md-6">
            <label className="form-label">Price</label>

            <input
              type="text"
              name="price"
              value={priceInput}
              onChange={(e) => {
                const value = e.target.value;
                if (value === "" || /^\d*\.?\d{0,2}$/.test(value)) {
                  setPriceInput(value);
                  setFormData({
                    ...formData,
                    price:
                      value === ""
                        ? 0
                        : parseFloat(parseFloat(value).toFixed(2)),
                  });
                }
              }}
              className="form-control"
              placeholder="0.00"
              required
              aria-label="Enter the price for the listing"
            />
          </div>

          <div className="col-md-6">
            <label className="form-label">Category</label>
            <select
              name="category"
              value={formData.category}
              onChange={handleInputChange}
              className="form-control"
              required
              aria-label="Select a category for the listing"
            >
              <option value="">Select category</option>
              {categories.map((category) => (
                <option key={category} value={category}>
                  {category}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* condition dropdown */}
        <div className="mb-3">
          <label className="form-label">Condition</label>
          <select
            name="condition"
            value={formData.condition}
            onChange={handleInputChange}
            className="form-control"
            required
            aria-label="Select the condition of the listing"
          >
            <option value="">Select condition</option>
            {conditions.map((condition) => (
              <option key={condition} value={condition}>
                {condition}
              </option>
            ))}
          </select>
        </div>

        {/* tags input */}
        <div className="mb-3">
          <label className="form-label">Tags</label>
          <div className="tag-container">
            {formData.tags.map((tag, i) => (
              <span
                key={i}
                className="badge bg-secondary me-2"
                aria-label="Remove tag"
              >
                {tag}
                <button
                  key={i}
                  type="button"
                  onClick={() => removeTag(tag)}
                  className="btn-close btn-close-white ms-2"
                  aria-label={`Remove tag ${tag}`}
                />
              </span>
            ))}
          </div>
          <input
            type="text"
            name="tags"
            value={tagInput}
            onChange={(e) => setTagInput(e.target.value)}
            onKeyDown={handleTagKeyDown}
            className="form-control"
            placeholder="Type and press Enter to add tags"
            aria-label="Enter tags for the listing, press Enter to add"
          />
        </div>

        {/* image upload and preview */}
        <div className="mb-3">
          <label className="form-label">Image</label>

          {/* Show existing image preview if editing and imageUrl exists */}
          {isEditing && formData.imageUrl && (
            <div className="image-preview-container">
              <img
                src={formData.imageUrl}
                alt="Existing Image"
                className="img-thumbnail mb-2"
                style={{ maxWidth: "200px", maxHeight: "200px" }}
                aria-label="Preview of the existing uploaded image"
              />
              <button
                type="button"
                className="btn btn-danger btn-sm"
                onClick={() =>
                  setFormData((prev) => ({ ...prev, imageUrl: "", images: [] }))
                }
                aria-label="Remove existing image"
              >
                Remove Image
              </button>
            </div>
          )}

          {/* File input for uploading new images */}
          <input
            type="file"
            name="image"
            accept="image/*"
            multiple
            onChange={handleImageUpload}
            className="form-control"
            required={!isEditing || !formData.imageUrl} // Only required if not editing or no imageUrl exists
            aria-label="Upload images for the listing"
          />

          {/* Preview newly uploaded images */}
          <div className="image-preview-container d-flex flex-wrap gap-2 mt-2">
            {formData.images.map((image, index) => (
              <div key={index} className="position-relative">
                <img
                  src={URL.createObjectURL(image)}
                  alt={`Preview ${index}`}
                  className="img-thumbnail"
                  style={{ maxWidth: "200px", maxHeight: "200px" }}
                />
                <button
                  type="button"
                  className="btn-close position-absolute top-0 end-0"
                  aria-label={`Remove uploaded image ${index + 1}`}
                  onClick={() => removeImage(index)}
                ></button>
              </div>
            ))}
          </div>
        </div>

        {/* submit button */}
        <button
          type="submit"
          className="btn btn-submit w-100"
          aria-label={
            isEditing ? "Save changes to the listing" : "Create new listing"
          }
        >
          {isEditing ? "Save changes" : "Create listing"}
        </button>
      </form>
    </div>
  );
};

export default ListItemPopup;
