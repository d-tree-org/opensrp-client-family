package org.smartregister.family.fragment;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.BaseUnitTest;
import org.smartregister.family.R;
import org.smartregister.family.activity.BaseFamilyRegisterActivity;
import org.smartregister.family.util.Utils;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.contract.BaseRegisterFragmentContract;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.smartregister.view.fragment.SecuredNativeSmartRegisterFragment.DIALOG_TAG;

/**
 * Created by samuelgithengi on 3/24/20.
 */
public class BaseFamilyRegisterFragmentTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Context context;

    @Mock
    private CommonRepository commonRepository;

    @Mock
    public RecyclerView clientsView;

    @Mock
    private BaseRegisterFragmentContract.Presenter presenter;

    @Mock
    private ProgressBar syncProgressBar;

    @Mock
    private ImageView syncButton;

    @Mock
    private BaseFamilyRegisterActivity baseFamilyRegisterActivity;

    @Mock
    private FragmentManager fragmentManager;

    @Mock
    private FragmentTransaction fragmentTransaction;

    @Captor
    private ArgumentCaptor<RecyclerViewPaginatedAdapter> adapterArgumentCaptor;

    private BaseFamilyRegisterFragment registerFragment;

    private AppCompatActivity activity;

    @Before
    public void setUp() {
        registerFragment = Mockito.mock(BaseFamilyRegisterFragment.class, Mockito.CALLS_REAL_METHODS);
        CoreLibrary.init(context);
        when(context.commonrepository(anyString())).thenReturn(commonRepository);
        activity = Robolectric.buildActivity(AppCompatActivity.class).create().resume().get();
        when(registerFragment.getActivity()).thenReturn(activity);
        when(registerFragment.getContext()).thenReturn(activity);
        Context.bindtypes = new ArrayList<>();
        Whitebox.setInternalState(registerFragment, "clientsView", clientsView);
        Whitebox.setInternalState(registerFragment, "presenter", presenter);
    }

    @Test
    public void testInitializeAdapter() {
        registerFragment.initializeAdapter(new HashSet<View>());
        verify(clientsView).setAdapter(adapterArgumentCaptor.capture());
        assertNotNull(adapterArgumentCaptor.getValue());
        assertEquals(20, adapterArgumentCaptor.getValue().currentlimit);

    }

    @Test
    public void testSetupViews() {
        android.view.View view = LayoutInflater.from(activity).inflate(R.layout.fragment_base_register, null);
        registerFragment.setupViews(view);
        assertEquals(R.color.white, Shadows.shadowOf(registerFragment.getSearchView().getBackground()).getCreatedFromResId());
        assertEquals(activity.getString(R.string.sort), ((TextView) view.findViewById(R.id.filter_text_view)).getText());
        assertEquals(android.view.View.GONE, view.findViewById(R.id.opensrp_logo_image_view).getVisibility());


        CustomFontTextView titleView = view.findViewById(R.id.txt_title_label);
        assertEquals(android.view.View.VISIBLE, titleView.getVisibility());
        assertEquals(activity.getString(R.string.all_families), titleView.getText());
    }

    @Test
    public void testRefreshSyncProgressSpinner() {
        Whitebox.setInternalState(registerFragment, "syncProgressBar", syncProgressBar);
        Whitebox.setInternalState(registerFragment, "syncButton", syncButton);
        SyncStatusBroadcastReceiver.init(activity);
        registerFragment.refreshSyncProgressSpinner();
        verify(syncButton).setVisibility(android.view.View.GONE);
    }

    @Test
    public void testSetUniqueID() {
        when(registerFragment.getSearchView()).thenReturn(new EditText(activity));
        registerFragment.setUniqueID("11223");
        assertEquals("11223", registerFragment.getSearchView().getText().toString());
    }

    @Test
    public void testStartRegistrationStartsFormActivity() {
        Utils.metadata().updateFamilyRegister("register_family.json", "ec_family", "", "", "", "", "");
        when(registerFragment.getActivity()).thenReturn(baseFamilyRegisterActivity);
        registerFragment.startRegistration();
        verify(baseFamilyRegisterActivity).startFormActivity("register_family.json", null, null);
    }

    @Test
    public void testShowNotFoundPopupShowsDialog() {
        when(registerFragment.getActivity()).thenReturn(baseFamilyRegisterActivity);
        when(baseFamilyRegisterActivity.getFragmentManager()).thenReturn(fragmentManager);
        when(fragmentManager.beginTransaction()).thenReturn(fragmentTransaction);
        registerFragment.showNotFoundPopup("1234");
        verify(fragmentManager).beginTransaction();
        verify(fragmentTransaction).addToBackStack(null);
        verify(fragmentTransaction).add(any(NoMatchDialogFragment.class), eq(DIALOG_TAG));
    }
}
