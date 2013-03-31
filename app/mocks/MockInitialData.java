/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * the license agreement granted to you by GigaSpaces.
 */

package mocks;

import bootstrap.InitialData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: guym
 * Date: 3/31/13
 * Time: 5:33 PM
 */
public class MockInitialData implements InitialData {

    private static Logger logger = LoggerFactory.getLogger( MockInitialData.class );

    @Override
    public void load( String data )
    {
        logger.info("MOCK : initial data");
    }
}
