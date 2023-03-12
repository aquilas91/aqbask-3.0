package test1234;

import java.sql.*;
import java.util.Scanner;

public class AquilabaskInventorySystem {
    private static final String URL = "jdbc:mysql://localhost:3306/aquilabaskinventorysystem";
    private static final String USER = "eclipse";
    private static final String PASSWORD = "Testing123";

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            Scanner scanner = new Scanner(System.in);
            int choice = 0;
            while (choice != 6) {
                System.out.println("1. Add product");
                System.out.println("2. Update product quantity");
                System.out.println("3. Retrieve product information");
                System.out.println("4. Place order");
                System.out.println("5. Delete order");
                System.out.println("6. Retrieve order information");
                System.out.println("7. Exit");
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        addProduct(conn, scanner);
                        break;
                    case 2:
                        updateProductQuantity(conn, scanner);
                        break;
                    case 3:
                        retrieveProductInformation(conn, scanner);
                        break;
                    case 4:
                        placeOrder(conn, scanner);
                        break;
                    case 5:
                    	deleteOrder(conn, scanner);
                        break;
                    case 6:
                        retrieveOrderInformation(conn, scanner);
                        break;
                    case 7:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        break;
                }
            }

            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void addProduct(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter product description: ");
        String description = scanner.nextLine();
        System.out.print("Enter product quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter product price: ");
        double price = scanner.nextDouble();

        String insertQuery = "INSERT INTO products (name, description, quantity, price) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(insertQuery);
        pstmt.setString(1, name);
        pstmt.setString(2, description);
        pstmt.setInt(3, quantity);
        pstmt.setDouble(4, price);
        pstmt.executeUpdate();

        System.out.println("Product added successfully.");
    }

    private static void updateProductQuantity(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new quantity: ");
        int quantity = scanner.nextInt();

        String updateQuery = "UPDATE products SET quantity = ? WHERE name = ?";
        PreparedStatement pstmt = conn.prepareStatement(updateQuery);
        pstmt.setInt(1, quantity);
        pstmt.setString(2, name);
        int rowsUpdated = pstmt.executeUpdate();

        if (rowsUpdated > 0) {
            System.out.println("Product quantity updated successfully.");
        } else {
            System.out.println("Product not found.");
        }
    }

    private static void retrieveProductInformation(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();

        String selectQuery = "SELECT * FROM products WHERE name = ?";
        PreparedStatement pstmt = conn.prepareStatement(selectQuery);
        pstmt.setString(1, name);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            String description = rs.getString("description");
            int quantity = rs.getInt("quantity");
            double price = rs.getDouble("price");
            System.out.printf("ID: %d%n", id);
            System.out.printf("Name: %s%n", name);
            System.out.printf("Description: %s%n", description);
            System.out.printf("Quantity: %d%n", quantity);
            System.out.printf("Price: %.2f%n", price);
        } else {
            System.out.println("Product not found.");
        }
    }
    
    private static void placeOrder(Connection conn, Scanner scanner) throws SQLException {
        // Get customer information
        System.out.print("Enter customer name: ");
        String customerName = scanner.nextLine();
        
        // Create order in orders table
        String insertOrderQuery = "INSERT INTO orders (customer_name) VALUES (?)";
        PreparedStatement pstmt1 = conn.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS);
        pstmt1.setString(1, customerName);
        pstmt1.executeUpdate();

        // Get the generated order id
        ResultSet generatedKeys = pstmt1.getGeneratedKeys();
        int orderId = 0;
        if (generatedKeys.next()) {
            orderId = generatedKeys.getInt(1);
        } else {
            System.err.println("Error creating order.");
            return;
        }

        // Get product information for order items
        int choice = 0;
        while (choice != 2) {
            System.out.println("1. Add product to order");
            System.out.println("2. Done");
            System.out.print("Enter choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Enter product name: ");
                String productName = scanner.nextLine();
                System.out.print("Enter quantity: ");
                int quantity = scanner.nextInt();

                // Get product information from products table
                String selectQuery = "SELECT * FROM products WHERE name = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(selectQuery);
                pstmt2.setString(1, productName);
                ResultSet rs = pstmt2.executeQuery();

                if (rs.next()) {
                    int productId = rs.getInt("id");
                    int productQuantity = rs.getInt("quantity");
                    double productPrice = rs.getDouble("price");

                    // Check if there is enough quantity
                    if (productQuantity < quantity) {
                        System.out.println("Insufficient quantity.");
                    } else {
                        // Create order item in order_items table
                        String insertOrderItemQuery = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
                        PreparedStatement pstmt3 = conn.prepareStatement(insertOrderItemQuery);
                        pstmt3.setInt(1, orderId);
                        pstmt3.setInt(2, productId);
                        pstmt3.setInt(3, quantity);
                        pstmt3.executeUpdate();

                        // Update product quantity in products table
                        int updatedQuantity = productQuantity - quantity;
                        String updateProductQuery = "UPDATE products SET quantity = ? WHERE id = ?";
                        PreparedStatement pstmt4 = conn.prepareStatement(updateProductQuery);
                        pstmt4.setInt(1, updatedQuantity);
                        pstmt4.setInt(2, productId);
                        pstmt4.executeUpdate();

                        // Print order item details
                        double totalPrice = productPrice * quantity;
                        System.out.printf("Product: %s\nQuantity: %d\nPrice: $%.2f\nTotal Price: $%.2f\n", productName, quantity, productPrice, totalPrice);
                    }
                } else {
                    System.out.println("Product not found.");
                }
            }
        }

        System.out.println("Order placed successfully.");
    }
    
    private static void deleteOrder(Connection conn, Scanner scanner) throws SQLException {
    	System.out.print("Enter order ID: ");
    	int orderId = scanner.nextInt();
    	scanner.nextLine();
    	// Check if order exists
    	String selectQuery = "SELECT * FROM orders WHERE id = ?";
    	PreparedStatement pstmt1 = conn.prepareStatement(selectQuery);
    	pstmt1.setInt(1, orderId);
    	ResultSet rs = pstmt1.executeQuery();

    	if (!rs.next()) {
    	    System.out.println("Order not found.");
    	    return;
    	}

    	// Delete order items
    	String deleteOrderItemsQuery = "DELETE FROM order_items WHERE order_id = ?";
    	PreparedStatement pstmt2 = conn.prepareStatement(deleteOrderItemsQuery);
    	pstmt2.setInt(1, orderId);
    	pstmt2.executeUpdate();

    	// Delete order
    	String deleteOrderQuery = "DELETE FROM orders WHERE id = ?";
    	PreparedStatement pstmt3 = conn.prepareStatement(deleteOrderQuery);
    	pstmt3.setInt(1, orderId);
    	pstmt3.executeUpdate();

    	System.out.println("Order deleted successfully.");
    	}

		private static void retrieveOrderInformation(Connection conn, Scanner scanner) throws SQLException {
		System.out.print("Enter order ID: ");
		int orderId = scanner.nextInt();
		// Retrieve order information from orders table
		String selectOrderQuery = "SELECT * FROM orders WHERE id = ?";
		PreparedStatement pstmt1 = conn.prepareStatement(selectOrderQuery);
		pstmt1.setInt(1, orderId);
		ResultSet rs1 = pstmt1.executeQuery();

		if (rs1.next()) {
		    String customerName = rs1.getString("customer_name");
		    System.out.printf("Order ID: %d%nCustomer name: %s%n", orderId, customerName);
		} else {
		    System.out.println("Order not found.");
		    return;
		}

		// Retrieve order items from order_items table
		String selectItemsQuery = "SELECT p.name, i.quantity, p.price FROM order_items i JOIN products p ON i.product_id = p.id WHERE i.order_id = ?";
		PreparedStatement pstmt2 = conn.prepareStatement(selectItemsQuery);
		pstmt2.setInt(1, orderId);
		ResultSet rs2 = pstmt2.executeQuery();

		double totalCost = 0;
		while (rs2.next()) {
		    String productName = rs2.getString("name");
		    int quantity = rs2.getInt("quantity");
		    double price = rs2.getDouble("price");
		    double itemCost = quantity * price;
		    totalCost += itemCost;
		    System.out.printf("%s x %d @ $%.2f ea = $%.2f%n", productName, quantity, price, itemCost);
		}

		System.out.printf("Total cost: $%.2f%n", totalCost);
		}
		
		
}
		
	























        