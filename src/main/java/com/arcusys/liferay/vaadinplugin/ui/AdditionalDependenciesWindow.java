package com.arcusys.liferay.vaadinplugin.ui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class AdditionalDependenciesWindow extends Window {

    private final IndexedContainer dependencyContainer = new IndexedContainer();
    private final TextField filterTextField;
    private final AbstractSelect dependenciesSelector;
    private Button closeButton;

    private SimpleStringFilter filter = null;

    public AdditionalDependenciesWindow(List<File> files,
                                        List<File> includedDependencies) {
        setCaption("Additional dependencies (WEB-INF/lib)");
        setWidth("350px");
        setHeight("350px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);

        dependencyContainer.addContainerProperty("caption", String.class, null);

        filterTextField = createFilterTextField();
        layout.addComponent(filterTextField);

        dependenciesSelector = createDependenciesSelector();
        populateSelector(files, includedDependencies);
        layout.addComponent(dependenciesSelector);
        layout.setExpandRatio(dependenciesSelector, 1);

        layout.addComponent(createCloseButton());
    }

    private TwinColSelect createDependenciesSelector() {
        TwinColSelect dependenciesSelector = new TwinColSelect(null,
                dependencyContainer);
        dependenciesSelector.setImmediate(true);
        dependenciesSelector.setMultiSelect(true);
        dependenciesSelector.setSizeFull();
        dependenciesSelector.setLeftColumnCaption("Available");
        dependenciesSelector.setRightColumnCaption("Included");
        return dependenciesSelector;
    }

    private TextField createFilterTextField() {
        TextField filterTextField = new TextField("Filter");
        filterTextField.setImmediate(true);
        filterTextField.addTextChangeListener(new FieldEvents.TextChangeListener() {
            public void textChange(FieldEvents.TextChangeEvent event) {
                filterDependencies(event.getText());
            }
        });
        filterTextField.setWidth("100%");
        filterTextField.focus();
        return filterTextField;
    }

    private void filterDependencies(String filterString) {
        Container.Filterable f = dependencyContainer;

        // remove old filterTextField
        if (filter != null) {
            f.removeContainerFilter(filter);
        }

        // set new filterTextField
        if (filterString == null || "".equals(filterString)) {
            filter = null;
        } else {
            filter = new SimpleStringFilter("caption", filterString, true,
                    false);
            f.addContainerFilter(filter);
        }
    }

    private Button createCloseButton() {
        closeButton = new Button("Close", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        return closeButton;
    }

    public void populateSelector(List<File> files,
                                 List<File> includedDependencies) {
        if (files != null) {
            Collections.sort(files);

            dependencyContainer.removeAllItems();
            dependenciesSelector.setItemCaptionPropertyId("caption");

            for (File file : files) {
                Item item = dependencyContainer.addItem(file);
                item.getItemProperty("caption").setValue(file.getName());
            }

            if (includedDependencies != null) {
                dependenciesSelector.setValue(includedDependencies);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<File> getAdditionalDependencies() {
        List<File> files = new ArrayList<File>();
        for (Object o : (Set<Object>) dependenciesSelector.getValue()) {
            if (o instanceof File) {
                files.add((File) o);
            }
        }
        Collections.sort(files);
        return files;
    }
}
