package test;

import static org.junit.Assert.*;

import org.junit.Test;

import application.Invoker;

public class testInvoker {
	@Test
	public void	invokerCreatedCorrectly()
	{
		Invoker	inv;
		long 	maxValue = Long.MAX_VALUE;

		inv = Invoker.createInvoker(1);
		assertEquals(inv.getMaxRam(), 1);
		assertEquals(inv.getRamUsed(), 0);

		inv = Invoker.createInvoker(42);
		assertEquals(inv.getMaxRam(), 42);
		assertEquals(inv.getRamUsed(), 0);

		inv = Invoker.createInvoker(maxValue);
		assertEquals(inv.getMaxRam(), maxValue);
		assertEquals(inv.getRamUsed(), 0);

		inv = Invoker.createInvoker(0);
		assertEquals(inv, null);

		inv = Invoker.createInvoker(-1);
		assertEquals(inv, null);
	}

	@Test
	public void    invokerErasedCorrectly()
	{
		
	}

}
