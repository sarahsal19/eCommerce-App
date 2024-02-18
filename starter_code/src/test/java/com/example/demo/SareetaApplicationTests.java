package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import com.example.demo.model.persistence.Cart;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SareetaApplicationTests {
	private UserController userController;
	private ItemController itemController;
	private CartController cartController;
	private OrderController orderController;
	private UserRepository userRepository = mock(UserRepository.class);
	private ItemRepository itemRepository = mock(ItemRepository.class);
	private CartRepository cartRepository = mock(CartRepository.class);
	private OrderRepository orderRepository = mock(OrderRepository.class);
	private BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);

	@Before
	public void setup() {
		userController = new UserController();
		itemController = new ItemController();
		orderController = new OrderController();
		cartController = new CartController();

		injectObjects(userController, "userRepository", userRepository);
		injectObjects(userController, "cartRepository", cartRepository);
		injectObjects(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);
		injectObjects(itemController, "itemRepository", itemRepository);
		injectObjects(cartController, "userRepository", userRepository);
		injectObjects(cartController, "cartRepository", cartRepository);
		injectObjects(cartController, "itemRepository", itemRepository);
		injectObjects(orderController, "userRepository", userRepository);
		injectObjects(orderController, "orderRepository", orderRepository);
	}

	@Test
	public void createUserTest() throws Exception {

		when(bCryptPasswordEncoder.encode("rawPassword")).thenReturn("hashedPassword");

		CreateUserRequest createUserRequest = new CreateUserRequest();
		createUserRequest.setUsername("test");
		createUserRequest.setPassword("rawPassword");
		createUserRequest.setConfirmedPassword("rawPassword");

		final ResponseEntity<User> response = userController.createUser(createUserRequest);
		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());

		User createdUser = response.getBody();
		assertEquals(0, createdUser.getId());
		assertEquals("test", createdUser.getUsername());
		assertEquals("hashedPassword", createdUser.getPassword());

		createUserRequest.setUsername("Afnan");
		userController.createUser(createUserRequest);
		ResponseEntity<User> testAfnanUser = userController.findByUserName("Afnan");
		User randomUser = userController.findByUserName("randomUser").getBody();
		assertNull("randomUser", randomUser);

	}

	@Test
	public void itemControllerTest() {
		Item items = new Item();
		items.setDescription("Book");
		items.setId(0L);
		items.setName("The Power of Now");
		items.setPrice(new BigDecimal(50.00));
		List<Item> itemList = new ArrayList<>();
		itemList.add(items);
		when(itemRepository.findById(anyLong())).thenReturn(java.util.Optional.of(items));
		ResponseEntity<Item> responseEntity = itemController.getItemById(0L);
		assertNotNull(responseEntity);
		assertEquals(200, responseEntity.getStatusCodeValue());

		when(itemRepository.findByName(anyString())).thenReturn(itemList);
		ResponseEntity<List<Item>> responseEntityList = itemController.getItemsByName(items.getName());
		assertNotNull(responseEntityList);
		assertEquals(200, responseEntityList.getStatusCodeValue());
	}

	@Test
	public void cartControllerTest() throws IOException {
		User user = new User();
		user.setUsername("Noura");
		Cart cart = new Cart();
		user.setCart(cart);

		Item items = new Item();
		items.setDescription("Charger");
		items.setId(0L);
		items.setName("Iphone charger");
		items.setPrice(new BigDecimal(120.00));

		cart.addItem(items);

		ModifyCartRequest modifyCartRequest = new ModifyCartRequest();
		modifyCartRequest.setItemId(0L);
		modifyCartRequest.setUsername("test");
		modifyCartRequest.setQuantity(1);
		when(userRepository.findByUsername(anyString())).thenReturn(user);
		when(itemRepository.findById(anyLong())).thenReturn(Optional.of(items));
		ResponseEntity<Cart> responseEntity = cartController.addTocart(modifyCartRequest);
		assertNotNull(responseEntity);
		assertEquals(200, responseEntity.getStatusCodeValue());
		responseEntity = cartController.removeFromcart(modifyCartRequest);
		assertNotNull(responseEntity);
		assertEquals(200, responseEntity.getStatusCodeValue());

	}

	@Test
	public void orderControllerTest() {
		User user = new User();
		user.setUsername("Sarah");
		when(userRepository.findByUsername(anyString())).thenReturn(user);
		Cart cart = new Cart();
		Item items = new Item();
		items.setDescription("Plant");
		items.setId(0L);
		items.setName("cactus");
		items.setPrice(new BigDecimal(30.00));
		cart.addItem(items);
		user.setCart(cart);
		ResponseEntity<UserOrder> userOrderResponseEntity = orderController.submit(user.getUsername());
		assertNotNull(userOrderResponseEntity);
		assertEquals(200, userOrderResponseEntity.getStatusCodeValue());
		ResponseEntity<List<UserOrder>> ordersList = orderController.getOrdersForUser(user.getUsername());
		assertNotNull(ordersList);
		assertEquals(200, ordersList.getStatusCodeValue());

	}

	private void injectObjects(Object target, String fieldName, Object toInject) {

		boolean wasPrivate = false;

		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			if(!field.isAccessible()){
				field.setAccessible(true);
				wasPrivate = true;
			}

			field.set(target, toInject);
			if(wasPrivate){
				field.setAccessible(false);
			}

		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
