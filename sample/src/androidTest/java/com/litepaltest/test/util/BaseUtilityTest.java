package com.litepaltest.test.util;

import org.litepal.util.BaseUtility;

import com.litepaltest.test.LitePalTestCase;

public class BaseUtilityTest extends LitePalTestCase{

	public void testCount() {
		String string = " This is a good one. That is a bad one. ";
		String markThis = "This";
		String markIs = "is";
		String markA = "a";
		String markGood = "good";
		String markOne = "one";
		String markPoint = ".";
		String markSpace = " ";
		String markThat = "That";
		String markBad = "bad";
		String markNone = "none";
		String markEmpty = "";
		String markNull = null;
		assertEquals(1, BaseUtility.count(string, markThis));
		assertEquals(3, BaseUtility.count(string, markIs));
		assertEquals(4, BaseUtility.count(string, markA));
		assertEquals(1, BaseUtility.count(string, markGood));
		assertEquals(2, BaseUtility.count(string, markOne));
		assertEquals(2, BaseUtility.count(string, markPoint));
		assertEquals(11, BaseUtility.count(string, markSpace));
		assertEquals(1, BaseUtility.count(string, markThat));
		assertEquals(1, BaseUtility.count(string, markBad));
		assertEquals(0, BaseUtility.count(string, markNone));
		assertEquals(0, BaseUtility.count(string, markEmpty));
		assertEquals(0, BaseUtility.count(string, markNull));
	}

}
