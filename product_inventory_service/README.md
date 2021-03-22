# Product inventory service
[![Services](https://img.shields.io/badge/%E2%AC%85-Back-green.svg)](../README.md)

- Product inventory contains at least two different categories of products with at least 3 products in each
- Registered users could buy products from system or other users
    - Users should be able to add/update/delete their products
    - Users should be able to see other users' products
    - Users should be able to buy other users' products if they have enough currency
    - Users should be able to see all products that they bought
    - Buyer can send a product back if in the proccess of dispute, the seller requested it

## Endpoints
1. List all categories
> (): List[category_data]
2. List all products in a category
> (category_id: ID): List[short_product_data]
3. View product in detail
> (product_id: ID): long_product_data
4. Add a product
> (product_creation_data): product_id
5. Update a product
> (product_id, product_creation_data): Ok
6. Delete a product
> (product_id): Ok
7. Buy other user's product
> (product_id): Ok
8. Return a bought product
> (product_id): Ok

## Used
- Play - same as for user management: better option for quick and optimal rescourse management, quick realization

----------------------------------------
