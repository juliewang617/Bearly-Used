import React, { useEffect, useState } from "react";
import ListItemPopup from "./ListItemPopup";
import { Modal } from "bootstrap";
import "../styles/HomePage.css";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import mockProducts from "../data/product";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  useSearchParams,
  useNavigate,
} from "react-router-dom";

const ITEMS_PER_PAGE = 8;
type SortOrder = "" | "PRICE_ASC" | "PRICE_DESC";

//backend structure for Listings
interface ListingItem {
  id: number;
  title: string;
  description: string;
  price: number;
  category: string;
  condition: string;
  image_url: string;
  available: boolean;
  tags: string[];
}

// defines the structure of a price range filter
interface Price {
  label: string | null;
  min: number | null;
  max: number | null;
}

/**
 * Renders the Home Page
 *
 * Displays listings and has listing filtering functionalities.
 * Can filter listings based on price, category, and search words.
 *
 * @returns {JSX.Element} A JSX element representing the Home Page.
 */
const HomePage: React.FC = () => {
  // lists all available categories for filtering
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

  // defines the available price ranges for filtering
  const priceRanges = [
    { label: "Free", min: 0, max: 0 },
    { label: "Less than $5", min: 0.01, max: 5 },
    { label: "$5 - $10", min: 5, max: 10 },
    { label: "$10 - $20", min: 10, max: 20 },
    { label: "$20 - $30", min: 20, max: 30 },
    { label: "$30+", min: 30, max: null },
  ];

  // initializes search parameters from the URL
  const [searchParams, setSearchParams] = useSearchParams();
  const [selectedCategory, setSelectedCategory] = useState<string>(
    searchParams.get("category") || ""
  );
  const [selectedPrice, setSelectedPrice] = useState<Price | null>(
    searchParams.get("priceLabel")
      ? {
          label: searchParams.get("priceLabel"),
          min: searchParams.get("priceMin")
            ? Number(searchParams.get("priceMin"))
            : null,
          max: searchParams.get("priceMax")
            ? Number(searchParams.get("priceMax"))
            : null,
        }
      : null
  );
  const [priceSort, setPriceSort] = useState<SortOrder>(
    (searchParams.get("priceSort") as SortOrder) || ""
  );
  const [currentPage, setCurrentPage] = useState(
    Number(searchParams.get("page")) || 1
  );
  const [searchQuery, setSearchQuery] = useState<string>(
    searchParams.get("search") || ""
  );
  const [tempSearchQuery, setTempSearchQuery] = useState<string>("");
  // Trigger search when Enter key is pressed
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      setCurrentPage(1);
      setSearchQuery(tempSearchQuery);
    }
  };

  const [allListings, setAllListings] = useState<ListingItem[]>([]);
  const [filteredListings, setFilteredListings] = useState<ListingItem[]>([]);

  // ----------------------------------PAGING LOGIC----------------------------------
  // Calculate total pages
  const totalPages = Math.ceil(filteredListings.length / ITEMS_PER_PAGE);

  // Get current page items
  const getCurrentItems = () => {
    const indexOfLastItem = currentPage * ITEMS_PER_PAGE;
    const indexOfFirstItem = indexOfLastItem - ITEMS_PER_PAGE;
    return filteredListings.slice(indexOfFirstItem, indexOfLastItem);
  };

  // Generate page numbers array
  const getPageNumbers = () => {
    let pages = [];
    for (let i = 1; i <= totalPages; i++) {
      pages.push(i);
    }
    return pages;
  };

  // Handle page change
  const handlePageChange = (pageNumber: number) => {
    setCurrentPage(pageNumber);
    // Scroll to top of the page
    window.scrollTo(0, 0);
  };

  // ----------------------------------FILTERING LISTINGS LOGIC----------------------------------
  // USED FOR MOCK DATA
  // const [filteredListings, setFilteredListings] = useState<ListingItem[]>(
  //   mockProducts.mockProducts
  // );

  //fetch all listings from backend
  useEffect(() => {
    const fetchListings = async () => {
      try {
        const response = await fetch("http://localhost:3232/get-listings");
        const data = await response.json();
        console.log(data);
        if (data.response_type === "success") {
          setAllListings(data.result);
          setFilteredListings(data.result);
        } else {
          console.error("Error fetching listings");
        }
      } catch (err) {
        console.error("Error fetching listings");
      }
    };
    fetchListings();
  }, []);

  // filter listings based on selected category and price range + search functionality
  useEffect(() => {
    const fetchAndFilterListings = async () => {
      const params = new URLSearchParams();
      let apiUrlBase = "http://localhost:3232/get-listings?";

      // category filtering
      if (selectedCategory) {
        params.set("category", selectedCategory);
        apiUrlBase += `category=${selectedCategory}&`;
      }

      // price filtering
      if (selectedPrice) {
        const { label, min, max } = selectedPrice;

        if (label === "Free") {
          // if it's "Free", we only want listings with price 0
          params.set("priceMin", "0");
          params.set("priceMax", "0");
          apiUrlBase += `minPrice=0&maxPrice=0&`;
        } else {
          params.set("priceLabel", selectedPrice.label || "");
          if (selectedPrice.min) {
            params.set("priceMin", selectedPrice.min.toString());
            apiUrlBase += `minPrice=${selectedPrice.min}&`;
          }
          if (selectedPrice.max) {
            params.set("priceMax", selectedPrice.max.toString());
            apiUrlBase += `maxPrice=${selectedPrice.max}&`;
          }
        }
      }

      if (priceSort) {
        params.set("priceSort", priceSort);
        apiUrlBase += `sorter=${priceSort}&`;
      }

      // search filtering
      params.set("page", currentPage.toString());
      setSearchParams(params);
      console.log("API URL:", apiUrlBase);

      // if there's no search query, fetch listings with only filters applied
      if (!searchQuery) {
        try {
          const response = await fetch(apiUrlBase);
          const data = await response.json();

          if (data.response_type === "success") {
            setFilteredListings(data.result);
          } else {
            console.error("Error fetching listings");
          }
        } catch (err) {
          console.error("Error fetching listings:", err);
        }
        return;
      }

      const titleApiUrl = `${apiUrlBase}${params.toString()}&title=${searchQuery}`;
      const tagsApiUrl = `${apiUrlBase}${params.toString()}&tags=${searchQuery}`;

      try {
        // get results for both title and tags
        const [titleResponse, tagsResponse] = await Promise.all([
          fetch(titleApiUrl),
          fetch(tagsApiUrl),
        ]);

        const titleData = await titleResponse.json();
        const tagsData = await tagsResponse.json();

        // check for successful responses
        if (
          titleData.response_type === "success" &&
          tagsData.response_type === "success"
        ) {
          // merge results and remove duplicates
          const mergedListings = [...titleData.result, ...tagsData.result];
          const uniqueListings = Array.from(
            new Map(mergedListings.map((item) => [item.id, item])).values()
          );

          setFilteredListings(uniqueListings);
        } else {
          console.error("Error fetching listings");
        }
      } catch (err) {
        console.error("Error fetching listings:", err);
      }
    };
    fetchAndFilterListings();
  }, [selectedCategory, selectedPrice, priceSort, currentPage, searchQuery]);

  // USED FOR MOCK DATA
  /* 
  // THIS IS WHAT THE OLD FILTER WAS BEFORE I CHANGED IT -JULIE 
      let filteredListings = allListings;

    if (selectedCategory) {
      filteredListings = filteredListings.filter(
        (item) => item.category === selectedCategory
      );
    }

    // Price range filter
    if (selectedPriceRange) {
      const range = priceRanges.find((r) => r.label === selectedPriceRange);
      if (range) {
        filteredListings = filteredListings.filter((item) => {
          if (range.max === null) return item.price >= range.min;
          return item.price >= range.min && item.price <= range.max;
        });
      }
    }

    // Price sorting
    if (priceSort !== "none") {
      filteredListings = [...filteredListings].sort((a, b) => {
        if (priceSort === "asc") {
          return a.price - b.price;
        } else {
          return b.price - a.price;
        }
      });
    }

    setFilteredListings(filteredListings);
    setCurrentPage(1); // resert to first page when filters/sort change
  */

  // useEffect(() => {
  //   let filtered = mockProducts.mockProducts;

  //   if (selectedCategory) {
  //     filtered = filtered.filter((item) => item.category === selectedCategory);
  //   }

  //   // Price range filter
  //   if (selectedPrice) {
  //     const range = priceRanges.find((r) => r === selectedPrice);
  //     if (range) {
  //       filtered = filtered.filter((item) => {
  //         if (range.max === null) return item.price >= range.min;
  //         return item.price >= range.min && item.price <= range.max;
  //       });
  //     }
  //   }

  //   // Price sorting
  //   if (priceSort !== "") {
  //     filtered = [...filtered].sort((a, b) => {
  //       if (priceSort === "PRICE_ASC") {
  //         return a.price - b.price;
  //       } else {
  //         return b.price - a.price;
  //       }
  //     });
  //   }

  //   setFilteredListings(filtered);
  //   setCurrentPage(1); // resert to first page when filters/sort change
  // }, [selectedCategory, selectedPrice, priceSort]);

  // renders Prices Dropdown. displays price ranges to filter displayed listings.
  const renderPricesDropdown = () => (
    <li className="nav-item dropdown">
      <button
        className="nav-link dropdown-toggle"
        data-bs-toggle="dropdown"
        id="DropdownPrice"
        aria-expanded="false"
        aria-label="Select price filter"
        style={{
          position: "relative",
          left: "20px",
        }}
      >
        {selectedPrice ? `Price: ${selectedPrice.label}` : "Price"}
      </button>
      <ul className="dropdown-menu" aria-labelledby="DropdownPrice">
        <li>
          <h6 className="dropdown-header" aria-label="Price Range">
            Price Range
          </h6>
        </li>
        {priceRanges.map((range) => (
          <li key={range.label}>
            <button
              className="dropdown-item"
              onClick={() => {
                setCurrentPage(1), setSelectedPrice(range);
              }}
            >
              {range.label}
            </button>
          </li>
        ))}

        <li>
          <hr className="dropdown-divider" />
        </li>

        <li>
          <h6 className="dropdown-header">Sort by Price</h6>
        </li>
        <li>
          <button
            className={`dropdown-item ${
              priceSort === "PRICE_ASC" ? "active" : ""
            }`}
            onClick={() => {
              setCurrentPage(1), setPriceSort("PRICE_ASC");
            }}
          >
            <i className="bi bi-arrow-up" aria-label="Low to high"></i> Low to
            High
          </button>
        </li>
        <li>
          <button
            className={`dropdown-item ${
              priceSort === "PRICE_DESC" ? "active" : ""
            }`}
            onClick={() => {
              setCurrentPage(1), setPriceSort("PRICE_DESC");
            }}
          >
            <i className="bi bi-arrow-down"></i> High to Low
          </button>
        </li>

        {(selectedPrice || priceSort !== "") && (
          <>
            <li>
              <hr className="dropdown-divider" />
            </li>
            <li>
              <button
                className="dropdown-item"
                onClick={() => {
                  setSelectedPrice(null);
                  setPriceSort("");
                  setCurrentPage(1);
                }}
              >
                Clear Price Filters
              </button>
            </li>
          </>
        )}
      </ul>
    </li>
  );

  // initialize Create Listing Modal
  useEffect(() => {
    const modalElement = document.getElementById("addListingModal");
    if (modalElement) {
      new Modal(modalElement);
    }
  }, []);

  const closeModal = () => {
    const modalElement = document.getElementById("addListingModal");
    if (modalElement) {
      const modalInstance = Modal.getInstance(modalElement);
      if (modalInstance) {
        modalInstance.dispose(); // erm idk if this actually is good
      }

      modalElement.style.display = "none";
      document.body.classList.remove("modal-open");
      document.body.style.removeProperty("padding-right");
      document.body.style.overflow = "initial";

      const backdrops = document.querySelectorAll(".modal-backdrop");
      backdrops.forEach((backdrop) => backdrop.remove());
    }
  };

  //redirects user to a product page via unique urls based on the product's id
  const navigate = useNavigate();
  const handleProductClick = (id: number) => {
    navigate(`/product/${id}?${searchParams.toString()}`);
  };

  return (
    <div>
      <nav className="navbar navbar-expand-lg" aria-label="Main navigation">
        <div className="container-fluid">
          <ul className="navbar-nav">
            <div className="dropdown-container">
              {/* Prices Dropdown */}
              {renderPricesDropdown()}

              {/* Categories Dropdown */}
              <li className="nav-item dropdown">
                <button
                  className="nav-link dropdown-toggle"
                  data-bs-toggle="dropdown"
                  id="DropdownCategory"
                  aria-expanded="false"
                  aria-label="Select category filter"
                  style={{
                    position: "relative",
                    left: "40px",
                  }}
                >
                  {selectedCategory || "Categories"}
                </button>
                <ul
                  className="dropdown-menu"
                  aria-labelledby="DropdownCategory"
                >
                  {categories.map((category) => (
                    <li key={category}>
                      <button
                        className="dropdown-item"
                        onClick={() => setSelectedCategory(category)}
                        aria-label={`Filter by category: ${category}`}
                      >
                        {category}
                      </button>
                    </li>
                  ))}
                  {selectedCategory && (
                    <>
                      <li>
                        <hr className="dropdown-divider" />
                      </li>
                      <li>
                        <button
                          className="dropdown-item"
                          aria-label="Clear category filter"
                          onClick={() => setSelectedCategory("")}
                        >
                          Clear Category Filter
                        </button>
                      </li>
                    </>
                  )}
                </ul>
              </li>

              {/* Clear Price and Category Filters */}
              {(selectedCategory || selectedPrice) && (
                <li className="nav-item">
                  <button
                    className="nav-link"
                    onClick={() => {
                      setSelectedCategory("");
                      setSelectedPrice(null);
                    }}
                  >
                    Clear All Filters
                  </button>
                </li>
              )}
            </div>
          </ul>

          {/* Search */}
          <div className="search-bar mx-4" aria-label="Search listings">
            <input
              type="text"
              placeholder="Search listings by title or tags..."
              value={tempSearchQuery}
              onChange={(e) => setTempSearchQuery(e.target.value)}
              className="form-control"
              onKeyDown={handleKeyDown}
              aria-label="Enter search query"
            />
            <button
              type="button"
              className="search-btn"
              onClick={() => {
                setCurrentPage(1), setSearchQuery(tempSearchQuery);
              }}
              aria-label="Press to search"
            >
              🔍
            </button>
            {searchQuery && (
              <button
                type="button"
                className="nav-link"
                onClick={() => {
                  navigate("/"), setSearchQuery(""), setTempSearchQuery("");
                }}
                aria-label="Clear search"
              >
                Clear Search
              </button>
            )}
          </div>

          {/* Create Listing Button */}
          <button
            type="button"
            className="create-listing"
            data-bs-toggle="modal"
            data-bs-target="#addListingModal"
            aria-label="Create new listing"
            font-family="DM Sans"
          >
            Create Listing
          </button>
        </div>
      </nav>

      {/* Display Filtered Listings */}
      <div className="homepage-listings" aria-label="Filtered product listings">
        {filteredListings.length > 0 ? (
          <div className="homepage-listings-grid">
            {getCurrentItems().map((item) => (
              <div
                key={item.id}
                className="homepage-listing cursor-pointer"
                onClick={() => handleProductClick(item.id)}
                style={{ cursor: "pointer" }}
                aria-label={`View product: ${item.title}`}
              >
                <div className="homepage-listing-image">
                  <img
                    src={item.image_url}
                    className="img-fluid rounded mb-3 product-image"
                    alt="Product"
                  />
                </div>
                <div
                  className="homepage-listing-title"
                  aria-label={`Product title: ${item.title}`}
                >
                  {item.title}
                </div>
                <div
                  className="homepage-listing-price"
                  aria-label={`Price: $${item.price}`}
                >
                  ${item.price}
                </div>
                <div
                  className="homepage-listing-category"
                  aria-label={`Category: ${item.category}`}
                >
                  {item.category}
                </div>
                <div
                  className="homepage-listing-description"
                  aria-label={`Description: ${item.description}`}
                >
                  {item.description}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="no-items-found" aria-label="No items found">
            <i className="bi bi-search"></i>
            <h3>No Items Found</h3>
            <p>Try adjusting your filters or search terms!</p>
          </div>
        )}
      </div>

      {/* Pagination for Listings */}
      {filteredListings.length > 0 && (
        <nav aria-label="Pagination for product listings" className="mt-4">
          <ul className="pagination justify-content-center">
            <li className={`page-item ${currentPage === 1 ? "disabled" : ""}`}>
              <button
                className="page-link"
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
                aria-label="Previous page"
              >
                Previous
              </button>
            </li>

            {getPageNumbers().map((number) => (
              <li
                key={number}
                className={`page-item ${
                  currentPage === number ? "active" : ""
                }`}
                aria-current={currentPage === number ? "page" : undefined}
              >
                <button
                  className="page-link"
                  onClick={() => handlePageChange(number)}
                  aria-label={`Go to page ${number}`}
                >
                  {number}
                </button>
              </li>
            ))}

            <li
              className={`page-item ${
                currentPage === totalPages ? "disabled" : ""
              }`}
            >
              <button
                className="page-link"
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                aria-label="Next page"
              >
                Next
              </button>
            </li>
          </ul>
        </nav>
      )}

      {/* Add Modal Markup */}
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
              aria-label="Close modal for adding a listing"
              style={{
                fontSize: "0.75rem",
                padding: "0.5rem",
                paddingLeft: "1rem",
                paddingTop: "1rem",
              }}
            ></button>

            <div className="modal-body">
              <ListItemPopup
                onSubmit={() => {
                  closeModal();
                  // Refresh listings after submission
                  const fetchListings = async () => {
                    try {
                      const response = await fetch(
                        "http://localhost:3232/get-listings"
                      );
                      const data = await response.json();
                      if (data.response_type === "success") {
                        setAllListings(data.result);
                        setFilteredListings(data.result);
                      }
                    } catch (err) {
                      console.error("Error fetching listings");
                    }
                  };
                  fetchListings();
                }}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default HomePage;
