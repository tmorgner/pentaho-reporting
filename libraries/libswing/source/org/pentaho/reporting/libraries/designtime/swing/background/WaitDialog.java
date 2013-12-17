/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.libraries.designtime.swing.background;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.designtime.swing.LibSwingUtil;
import org.pentaho.reporting.libraries.designtime.swing.Messages;

/**
 * A dialog which will indicate to the user that an operation is being performed and will provide them an
 * opportunity to try and cancel that operation. This will create and run in a separate thread so that the user
 * and the background operation will not have to wait for completion.
 */
public class WaitDialog extends JDialog
{
  private static final int PADDING = 8;
  private static final Log logger = LogFactory.getLog(WaitDialog.class);
  private List<CancelListener> cancelListeners = new ArrayList<CancelListener>();
  private JLabel message;

  public WaitDialog(final boolean allowCancel)
  {
    super();
    init(allowCancel);
  }

  public WaitDialog(final Dialog parent, final boolean allowCancel)
  {
    super(parent);
    init(allowCancel);
  }

  public WaitDialog(final Frame parent, final boolean allowCancel)
  {
    super(parent);
    init(allowCancel);
  }

  public void addCancelListener(final CancelListener listener)
  {
    cancelListeners.add(listener);
  }

  public void removeCancelListener(final CancelListener listener)
  {
    cancelListeners.remove(listener);
  }

  private void init(final boolean allowCancel)
  {
    logger.debug("Initializing the Wait Dialog");
    setModal(true);
    setTitle(Messages.getInstance().getString("WaitDialog.TITLE"));
    setLayout(new BorderLayout(PADDING, PADDING));

    final JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    message = new JLabel(Messages.getInstance().getString("WaitDialog.MESSAGE"));
    contentPanel.add(message);
    add(contentPanel, BorderLayout.CENTER);

    if (allowCancel)
    {
      final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, PADDING, PADDING));
      final JButton cancelButton = new JButton(new CancelActionListener());
      buttonPanel.add(cancelButton);
      add(buttonPanel, BorderLayout.SOUTH);
    }

    setResizable(false);
    pack();

    // Since the parent dialog has not yet been centered, we will just center on the screen
    LibSwingUtil.centerFrameOnScreen(this);
  }

  public void setMessage(final String message)
  {
    this.message.setText(message);
  }
  
  /**
   * Indicates that the thread processing is complete and this dialog should close
   */
  public void exit()
  {
    setVisible(false);
    dispose();
  }

  /**
   * Class which handles the processing of a cancel request. It will notify all listeners waiting to handle
   * the Cancel request.
   */
  private class CancelActionListener extends AbstractAction
  {
    /**
     * Defines an <code>Action</code> object with a default
     * description string and default icon.
     */
    private CancelActionListener()
    {
      putValue(Action.NAME, Messages.getInstance().getString("WaitDialog.CANCEL"));
    }

    public void actionPerformed(final ActionEvent e)
    {
      final CancelEvent event = new CancelEvent(this);
      for (final CancelListener cancelListener : cancelListeners)
      {
        try
        {
          if (cancelListener != null)
          {
            logger.debug("Passing cancel action along to cancel listener [" + cancelListener + "]");
            cancelListener.cancelProcessing(event);
          }
        }
        catch (final Throwable ignored)
        {
          logger.warn(Messages.getInstance().formatMessage("WaitDialog.CANCEL_EXCEPTION", ignored.getLocalizedMessage()));
        }
      }
    }
  }
}
