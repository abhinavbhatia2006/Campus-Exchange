# Dormex🎓🛒

> A P2P marketplace exclusively for any college community.

## 📖 Overview

Dormex is a hyperlocal marketplace exclusively for students in a college community.

## 💡 Motivation
Our main motivation was to fix the idea of selling products over in WhatsApp groups. So we decided to make an independent centralized application where items can be traded with trust and ease.

## ✨ Key Features

- **Verified Access:** Users have to register themselves as part of the college community and select their college then register themselves and be part of the community.
- **OTP Verification**: OTP verification sent over e-mail registered by the users with expiry of 2 minutes.
- **Login via token**: Users are given a random token upon every login visit which has a fixed duration (24 hours) of expiry after which (s)he has to log in again.
- **Item Listing**: Items can be listed by the users with price, quantity and image.
- **Image Uploading:** Images can be uploaded using HTTP multi-path request.
- **Item Delisting**: Items are automatically delisted after 1 year (10 minutes here for demo purpose) if not sold.
- **Filter Feature:** Items can be filtered based on the categories provided.
- **Claim Item:** Items listed can be claimed prior to which lister receives the notification of the claim.
- **Item relisting:** Lister has an option to re-list the item (after it has been claimed) if the deal was unsuccessful. This is followed by blocking the user for 7 days (for that particular item)[...]
- **Blocked User**: Users are blocked if the deal is not successful (item is relisted) even after them claiming the item (Unable to claim the same item for 7 days).
- **Notification Updates:** Claimers and Listers receive notifications for specific actions that are relevant to them.
- **LogFile Generation**: Every activity of the backend is tracked and listed in a LogFile which can be used for debugging, tracking user activity, security purposes.

## 🔌 Endpoints Description

Base URL: http://localhost:8080

### 🛡️ Authentication Controller
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/signup-request` | **Signup**: Initiates registration by accepting user details and delegating logic to AuthService. |
| `POST` | `/api/auth/verify-otp` | **Verify OTP**: Validates the One-Time Password to complete registration and create the user. |
| `POST` | `/api/auth/login` | **Login**: Authenticates user via email/password and returns session data. |
| `POST` | `/api/auth/logout` | **Logout**: Invalidates the current session token. |

### 📦 Item Controller (Requires active session token)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/items` | **Create Item**: Uploads an image and details to list a new item (Multipart request). |
| `GET` | `/api/items` | **Get Items**: Fetches all items, with optional filtering by `category` and `college`. |
| `GET` | `/api/items/{id}` | **Get Item by ID**: Retrieves detailed information for a specific item. |
| `GET` | `/api/items/listed` | **Get Listed Items**: Displays all items currently listed by the logged-in user. |

### 🤝 Claim Controller (Requires active session token)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/claims` | **Create Claim**: Allows a buyer to claim an item using the `ClaimRequest` body. |
| `PUT` | `/api/claims/accept` | **Accept Claim**: Allows the lister (seller) to accept a specific claim request. |
| `PUT` | `/api/claims/reject` | **Reject Claim**: Allows the lister to reject a claim request. |
| `PUT` | `/api/claims/relist` | **Relist Item**: Puts an item back on the market after a failed deal and blocks the claimer. |
| `PUT` | `/api/claims/item/{id}/complete`| **Complete Deal**: Marks a transaction as successfully completed. |

### 🔔 Notification Controller (Requires active session token)
| Method | Endpoint | Description                                                  |
| :--- | :--- |:-------------------------------------------------------------|
| `GET` | `/api/notifications/{userId}` | **Get Notifications**: Retrieves alerts for a specific user. |

## 🛠️ Tech Stack

- **Backend:** Spring Framework with Java
- **Dependencies:**
  - spring-boot-starter-mail
  - spring-boot-starter-web
  - lombok
  - spring-boot-starter-test
- **Data Storage:** JSON (file based)


## 🚀 Installation & Setup

1.  **Clone the repository**

    ```bash
    git clone https://github.com/abhinavbhatia2006/Dormex-Marketplace.git
    cd Dormex-Marketplace
    ```
2. **Check if Java is installed:**
    ```bash
    java --version
    ```
3. **If not installed:**
    ```bash
    winget install -e --id Oracle.JDK.25
    ```
4. **Copy the App.jar file to another directory outside the Dormex-Marketplace directory**
5. Open terminal in the directory containing App.jar file.
6. **Run the Application**
    ```bash
    java -jar App.jar
    ```

## 🔮 Future Scope

- **Front End**
- **Web Deployment**
- **Chat Feature and Payment Gateway**
- **Using Database for data management**

## 🤝 Contributing

- Taneesh Kamleshkumar Bhojawala (BT2024053)
- Omkumar Alpeshbhai Aghera (BT2024088)
- Aryan Viraj Khadgi (BT2024151)
- Abhinav Bhatia (BT2024156)

## 📜 License

Distributed under the MIT License. See [LICENSE](https://github.com/abhinavbhatia2006/Dormex-Marketplace/blob/main/LICENSE) for more information.
