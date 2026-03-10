# ShopEase — Frontend

Next.js 16 frontend for the ShopEase e-commerce application.

## Tech Stack

- **Next.js 16.1.6** (App Router)
- **React 19**
- **Tailwind CSS 4**
- **Axios** — HTTP client
- **React Hot Toast** — notifications
- **Lucide React** — icons

## Pages

| Route           | Description                        |
|-----------------|------------------------------------|
| `/`             | Product listing with filters       |
| `/products/[id]`| Product detail                     |
| `/login`        | User login                         |
| `/register`     | User registration                  |
| `/cart`         | Shopping cart                      |
| `/checkout`     | Order placement                    |
| `/orders`       | Order history                      |
| `/admin`        | Admin dashboard                    |

## Running Locally

```bash
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000)

Requires the backend running at `http://localhost:8080`.

## Environment Variables

Create `.env.local` for production backend URL:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Project Structure

```
frontend/
├── app/              # Next.js App Router pages
├── components/       # Navbar, ProductCard, ProductSkeleton
├── context/          # AuthContext, CartContext
└── lib/              # Axios instance, utility functions
```
