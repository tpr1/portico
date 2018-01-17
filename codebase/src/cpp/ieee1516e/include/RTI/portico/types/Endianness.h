#pragma once
/*
 *   Copyright 2017 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL) 
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *   
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */


/**
 * Describes the byte ordering of {@link BasicType} datatypes.
 */
enum Endianness
{
	LITTLE, 
	BIG
};
	

//
//class Endinanness{
//private:
//	
//	EndiannessType endianness;
//
//private:
//
//	/**
//	 * Convert the EndiannessType value to a string
//	 * 
//	 * @return endianness type as a string
//	 */
//	std::string endiannessToString();
//
//public:
//
//	Endinanness(const EndiannessType& type);
//
//
//	/**
//	 * Overload the ostream operator
//	 * 
//	 * @param os the name of this data type
//	 * @param endianness the endianness object to get output
//	 */
//	std::ostream& operator<<( std::ostream& os, const Endianness& endianness );
//
//	/**
//	 * Overload the equality operator. Check to see if two Endians are the same.
//	 * 
//	 * @param endianness A Endianness type to compare
//	 * @param otherEndianness A Endianness type to compare to
//	 */
//	bool operator==( const Endianness& endianness, const Endianness& otherEndianness );
//
//	
//	/**
//	* If the provided string matches (ignoring case) the name of either
//	* endianness type, that type is returned. Otherwise an exception is thrown
//	*/
//	Endianness fromFomString(const std::string& fomString ); //throws JConfigurationException
//}


 