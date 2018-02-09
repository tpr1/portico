/*
 *   Copyright 2012 The Portico Project
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
#ifndef EXAMPLECPPFEDERATE_H_
#define EXAMPLECPPFEDERATE_H_

#include "ExampleFedAmb.h"
#include "RTI/RTI1516.h"
using namespace rti1516e;
using namespace std;

#define READY_TO_RUN L"ReadyToRun"

class ExampleCPPFederate
{
	public:
		RTIambassadorEx *rtiamb;
		ExampleFedAmb *fedamb;

		// fom handles //


		// ATTRIBTE TESTS
		// enumerated type tester
		ObjectClassHandle      sodaHandle; 
		AttributeHandle        flavourHandle;
		// simple type tester
		ObjectClassHandle      employee;
		AttributeHandle        payRate; // dollar rate
		// array type tester 
		// fixed record type
		AttributeHandle        AddressBook; // AddressType : employee
		// variant record
		ObjectClassHandle      waiter; // 
		AttributeHandle        efficiency; // waiter value

		// PARAMETER TESTS
		InteractionClassHandle rootBeerServedHandle;
		ParameterHandle        rootBeerCheckHandle;

		// public methods //
		ExampleCPPFederate();
		virtual ~ExampleCPPFederate();
		void runFederate( std::wstring federateName );


	private:
		void initializeHandles();
		void waitForUser();
		void enableTimePolicy();
		void publishAndSubscribe();
		ObjectInstanceHandle registerObject();
		void updateAttributeValues( ObjectInstanceHandle objectHandle );
		void sendInteraction();
		void advanceTime( double timestep );
		void deleteObject( ObjectInstanceHandle objectHandle );
		double getLbts();
};

#endif /*EXAMPLECPPFEDERATE_H_*/
