package com.in28minutes.example.layering.business.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.in28minutes.example.layering.business.api.client.ClientBO;
import com.in28minutes.example.layering.business.impl.client.ClientBOImpl;
import com.in28minutes.example.layering.data.api.client.ClientDO;
import com.in28minutes.example.layering.data.api.client.ProductDO;
import com.in28minutes.example.layering.model.api.client.Amount;
import com.in28minutes.example.layering.model.api.client.Client;
import com.in28minutes.example.layering.model.api.client.Currency;
import com.in28minutes.example.layering.model.api.client.Product;
import com.in28minutes.example.layering.model.api.client.ProductType;
import com.in28minutes.example.layering.model.impl.client.AmountImpl;
import com.in28minutes.example.layering.model.impl.client.ClientImpl;
import com.in28minutes.example.layering.model.impl.client.ProductImpl;

@RunWith(MockitoJUnitRunner.class)
public class ClientBOMockitoTest {

	@Mock
	private ProductDO productDO;

	@Mock
	private ClientDO clientDO;

	@InjectMocks
	private ClientBO clientBO = new ClientBOImpl();

	@Captor
	ArgumentCaptor<Client> clientArgumentCaptured;

	private static final int DUMMY_CLIENT_ID = 1;

	@Test
	public void testClientProductSum() {

		List<Product> products = Arrays.asList(createProductWithAmount("5.0"),
				createProductWithAmount("6.0"));

		stub(productDO.getAllProducts(anyInt())).toReturn(products);

		assertAmountEquals(
				new AmountImpl(new BigDecimal("11.0"), Currency.EURO), clientBO
						.getClientProductsSum(DUMMY_CLIENT_ID));
	}

	private void assertAmountEquals(Amount expectedAmount, Amount actualAmount) {
		assertEquals(expectedAmount.getCurrency(), actualAmount.getCurrency());
		assertEquals(expectedAmount.getValue(), actualAmount.getValue());
	}

	private Product createProductWithAmount(String amount) {
		return new ProductImpl(100, "Product 15", ProductType.BANK_GUARANTEE,
				new AmountImpl(new BigDecimal(amount), Currency.EURO));
	}

	@Test
	public void saveChangedProducts_ProductIsInserted() {
		
		List<Product> screenProduct = Arrays.asList(createProduct());
		
		List<Product> DatabaseProduct = new ArrayList<Product>();
		
		when(productDO.getAllProducts(DUMMY_CLIENT_ID)).thenReturn(DatabaseProduct);
		
		clientBO.saveChangedProducts(DUMMY_CLIENT_ID, screenProduct);
		
		verify(productDO, times(1)).insertProduct(DUMMY_CLIENT_ID, screenProduct.get(0));
	}
	
	@Test
	public void saveChangedProducts_ProductDeleted() {
		
		Product fromDatabase = createProduct();
		List<Product> fromDatabaseProducts = Arrays.asList(fromDatabase);
		
		List<Product> screenProduct = new ArrayList<Product>();
		
		when(productDO.getAllProducts(DUMMY_CLIENT_ID)).thenReturn(fromDatabaseProducts);
		
		clientBO.saveChangedProducts(DUMMY_CLIENT_ID, screenProduct);
		verify(productDO).deleteProduct(DUMMY_CLIENT_ID, fromDatabase);
		
	}
	
	@Test
	public void saveChangedProducts_ProductUpdated() {
		
		Product screenOnProduct = createProductWithAmount("5.0");
		
		List<Product> databaseProduct = Arrays.asList(createProductWithAmount("6.0"));
		
		List<Product> screenOnProducts = Arrays.asList(screenOnProduct);
		
		when(productDO.getAllProducts(DUMMY_CLIENT_ID)).thenReturn(databaseProduct);
		clientBO.saveChangedProducts(DUMMY_CLIENT_ID, screenOnProducts);
		
		verify(productDO, times(1)).deleteProduct(DUMMY_CLIENT_ID, screenOnProduct);
	}
	
	@Test
	public void saveChangedProducts_ProductInScreenAndNotInDatabase_ProductIsInserted() {

		List<Product> screenProducts = Arrays.asList(createProduct());

		List<Product> emptyDatabaseProducts = new ArrayList<Product>();

		stub(productDO.getAllProducts(anyInt()))
				.toReturn(emptyDatabaseProducts);

		clientBO.saveChangedProducts(1, screenProducts);

		verify(productDO).insertProduct(1, screenProducts.get(0));
	}

	private Product createProduct() {
		return new ProductImpl(100, "Product 15", ProductType.BANK_GUARANTEE,
				new AmountImpl(new BigDecimal("5.0"), Currency.EURO));
	}

	@Test
	public void saveChangedProducts_ProductInScreenAndDatabase_IsUpdated() {
		Product screenProduct = createProductWithAmount("5.0");

		List<Product> databaseProducts = Arrays
				.asList(createProductWithAmount("6.0"));
		List<Product> screenProducts = Arrays.asList(screenProduct);

		stub(productDO.getAllProducts(anyInt())).toReturn(databaseProducts);

		clientBO.saveChangedProducts(1, screenProducts);

		verify(productDO).updateProduct(1, screenProduct);
	}

	@Test
	public void saveChangedProducts_ProductInDatabaseButNotInScreen_Deleted() {

		Product productFromDatabase = createProduct();

		List<Product> databaseProducts = Arrays.asList(productFromDatabase);
		List<Product> emptyScreenProducts = new ArrayList<Product>();

		stub(productDO.getAllProducts(anyInt())).toReturn(databaseProducts);

		clientBO.saveChangedProducts(1, emptyScreenProducts);

		verify(productDO).deleteProduct(1, productFromDatabase);
	}

	@Test
	public void calculateAndSaveClientProductSum1() {

		ClientImpl client = createClientWithProducts(
				createProductWithAmount("6.0"), createProductWithAmount("6.0"));

		clientBO.calculateAndSaveClientProductSum(client);

		verify(clientDO).saveClient(clientArgumentCaptured.capture());

		assertEquals(new BigDecimal("12.0"), clientArgumentCaptured.getValue()
				.getProductAmount());

	}

	private ClientImpl createClientWithProducts(Product... products) {
		ClientImpl client = new ClientImpl(0, null, null, null, Arrays
				.asList(products));
		return client;
	}

}