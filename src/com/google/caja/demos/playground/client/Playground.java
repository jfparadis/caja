// Copyright (C) 2009 Google Inc.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.caja.demos.playground.client;

import com.google.caja.demos.playground.client.ui.PlaygroundView;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Main entry point for GWT-based Caja playground
 *
 * @author Jasvir Nagra (jasvir@gmail.com)
 */
public class Playground implements EntryPoint, ValueChangeHandler<String> {
  private PlaygroundView gui;

  private final PlaygroundServiceAsync cajolingService =
    GWT.create(PlaygroundService.class);

  public void loadSource(String url) {
    loadSource(Window.Location.getHref(), url);
  }

  public void loadSource(String base, String url) {
    gui.setLoading(true);
    gui.setUrl(url);
    if (!url.equals(History.getToken())) {
      History.newItem(url);
    }
    cajolingService.fetch(base, url, new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        gui.setLoading(false);
        gui.addRuntimeMessage(caught.getMessage());
        gui.selectTab(PlaygroundView.Tabs.RUNTIME_WARNINGS);
      }

      public void onSuccess(String result) {
        gui.setLoading(false);
        gui.setOriginalSource(result);
        gui.selectTab(PlaygroundView.Tabs.SOURCE);
      }
    });
  }

  public void clearPolicy() {
    gui.setPolicySource("");
  }

  public void loadPolicy(String url) {
    gui.setLoading(true);
    gui.setPolicyUrl(url);
    cajolingService.fetch(Window.Location.getHref(), url,
        new AsyncCallback<String>() {
          public void onFailure(Throwable caught) {
            gui.setLoading(false);
            gui.addRuntimeMessage(caught.getMessage());
            gui.selectTab(PlaygroundView.Tabs.RUNTIME_WARNINGS);
          }

          public void onSuccess(String result) {
            gui.setLoading(false);
            gui.setPolicySource(result);
            gui.selectTab(PlaygroundView.Tabs.POLICY);
          }
        });
  }

  /**
   * @param uri not used.
   * @param debugMode not used.
   */
  public void cajole(String uri, String input, final String policy,
      boolean debugMode, final String opt_idClass) {
    gui.setLoading(true);
    gui.selectTab(PlaygroundView.Tabs.RENDER);
    gui.setRenderedResult(uri, policy, input, null, opt_idClass);
    gui.setLoading(false);
  }

  public void onValueChange(ValueChangeEvent<String> change) {
    String historyToken = change.getValue();
    if (null == historyToken || "".equals(historyToken))
      return;
    loadSource(historyToken);
  }

  public void onModuleLoad() {
    gui = new PlaygroundView(this);
    gui.setLoading(true);
    cajolingService.getBuildInfo(new AsyncCallback<String>() {
      public void onFailure(Throwable caught) {
        gui.setLoading(false);
        gui.addRuntimeMessage(caught.getMessage());
        gui.setVersion("Unknown");
      }

      public void onSuccess(String result) {
        gui.setLoading(false);
        gui.setVersion(result);
      }
    });
    History.addValueChangeHandler(this);
    History.fireCurrentHistoryState();
  }
}
