# User management service
- CRUD users
- Users have their wallets
  `registered users are provided with some significant amount of in-system currency`
- Username unique check
- Password constraints
- Password encryption (SHA-256?)
- Authentication with username and password
- Authorization for other services

## Endpoints
1. Home page (with sign up and sign in)
   (is_logged_in :Boolean): main_url     //supposed to work as filter
2. Registration page
> (): registartion_url
3. Login page
> (): login_url
4. All users list
> (): users_all_url
5. Personal user page
> (user_id: ID): personal_page_url

## Used
- Play - because it's one of the biggest services in the app, using a batteries-included framework might be a better option for quick and optimal rescourse management

---