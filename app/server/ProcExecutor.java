package server;

import org.apache.commons.exec.Executor;

/**
 * ****************************************************************************
 * Copyright (c) 2010 GigaSpaces Technologies Ltd. All rights reserved
 * <p/>
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 * User: guym
 * Date: 12/3/12
 * Time: 8:09 PM
 * *****************************************************************************
 */
public interface ProcExecutor extends Executor {
    String getId();

    String getPrivateServerIP();

    String getOutput();

    int getElapsedTimeMin();
}
