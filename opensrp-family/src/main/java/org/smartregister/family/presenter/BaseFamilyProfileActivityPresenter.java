package org.smartregister.family.presenter;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.family.contract.FamilyProfileActivityContract;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.TreeSet;

public class BaseFamilyProfileActivityPresenter implements FamilyProfileActivityContract.Presenter {

    protected WeakReference<FamilyProfileActivityContract.View> viewReference;

    protected FamilyProfileActivityContract.Model model;

    protected RegisterConfiguration config;

    protected String familyBaseEntityId;

    protected Set<org.smartregister.configurableviews.model.View> visibleColumns = new TreeSet<>();

    private String viewConfigurationIdentifier;

    public BaseFamilyProfileActivityPresenter(FamilyProfileActivityContract.View view, FamilyProfileActivityContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId) {
        this.viewReference = new WeakReference<>(view);
        this.model = model;
        this.viewConfigurationIdentifier = viewConfigurationIdentifier;
        this.config = model.defaultRegisterConfiguration();
        this.familyBaseEntityId = familyBaseEntityId;
    }

    @Override
    public void processViewConfigurations() {
        if (StringUtils.isBlank(viewConfigurationIdentifier)) {
            return;
        }

        ViewConfiguration viewConfiguration = model.getViewConfiguration(viewConfigurationIdentifier);
        if (viewConfiguration != null) {
            config = (RegisterConfiguration) viewConfiguration.getMetadata();
            setVisibleColumns(model.getRegisterActiveColumns(viewConfigurationIdentifier));
        }

        if (config.getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(config.getSearchBarText());
        }
    }

    @Override
    public void initializeQueries(String mainCondition) {
        String tableName = Utils.metadata().familyActivityRegister.tableName;

        String countSelect = model.countSelect(tableName, mainCondition);
        String mainSelect = model.mainSelect(tableName, mainCondition);

        getView().initializeQueryParams(tableName, countSelect, mainSelect);
        getView().initializeAdapter(visibleColumns);

        getView().countExecute();
        getView().filterandSortInInitializeQueries();
    }

    @Override
    public void startSync() {
        // TODO implement start sync
    }

    @Override
    public void searchGlobally(String uniqueId) {
        // TODO implement Global search
    }

    @Override
    public String getMainCondition() {
        return String.format(" %s = '%s' and %s is null ", DBConstants.KEY.OBJECT_RELATIONAL_ID, familyBaseEntityId, DBConstants.KEY.DATE_REMOVED);
    }

    @Override
    public String getDefaultSortQuery() {
        return DBConstants.KEY.DOD + ", " + DBConstants.KEY.DOB + " ASC ";
    }

    protected FamilyProfileActivityContract.View getView() {
        if (viewReference != null)
            return viewReference.get();
        else
            return null;
    }

    private void setVisibleColumns(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        this.visibleColumns = visibleColumns;
    }

    public void setModel(FamilyProfileActivityContract.Model model) {
        this.model = model;
    }

    public String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }

}