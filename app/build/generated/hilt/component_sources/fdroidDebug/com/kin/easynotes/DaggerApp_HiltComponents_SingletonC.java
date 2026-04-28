package com.kin.easynotes;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.kin.easynotes.data.local.database.NoteDatabaseProvider;
import com.kin.easynotes.data.repository.ImportExportRepository;
import com.kin.easynotes.data.repository.NoteRepositoryImpl;
import com.kin.easynotes.data.repository.SettingsRepositoryImpl;
import com.kin.easynotes.di.ApplicationModule_ProvideCoroutineScopeFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideEncryptionHelperFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideExecutorCoroutineDispatcherFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideHandlerFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideImportExportRepositoryFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideKeystoreManagerFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideMutexFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideNoteDatabaseProviderFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideNoteRepositoryFactory;
import com.kin.easynotes.di.ApplicationModule_ProvideSettingsRepositoryFactory;
import com.kin.easynotes.domain.usecase.ImportExportUseCase;
import com.kin.easynotes.domain.usecase.NoteUseCase;
import com.kin.easynotes.domain.usecase.SettingsUseCase;
import com.kin.easynotes.presentation.MainActivity;
import com.kin.easynotes.presentation.components.EncryptionHelper;
import com.kin.easynotes.presentation.components.GalleryObserver;
import com.kin.easynotes.presentation.screens.edit.model.EditViewModel;
import com.kin.easynotes.presentation.screens.edit.model.EditViewModel_HiltModules;
import com.kin.easynotes.presentation.screens.edit.model.EditViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.kin.easynotes.presentation.screens.edit.model.EditViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.kin.easynotes.presentation.screens.home.viewmodel.HomeViewModel;
import com.kin.easynotes.presentation.screens.home.viewmodel.HomeViewModel_HiltModules;
import com.kin.easynotes.presentation.screens.home.viewmodel.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.kin.easynotes.presentation.screens.home.viewmodel.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel;
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel_HiltModules;
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.kin.easynotes.security.KeystoreManager;
import com.kin.easynotes.widget.NotesWidgetActivity;
import com.kin.easynotes.widget.NotesWidgetActivity_MembersInjector;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.ExecutorCoroutineDispatcher;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DaggerApp_HiltComponents_SingletonC {
  private DaggerApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public App_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements App_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public App_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements App_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public App_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements App_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public App_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements App_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public App_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements App_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public App_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements App_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public App_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements App_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public App_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends App_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends App_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends App_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends App_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public void injectNotesWidgetActivity(NotesWidgetActivity notesWidgetActivity) {
      injectNotesWidgetActivity2(notesWidgetActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(3).put(EditViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, EditViewModel_HiltModules.KeyModule.provide()).put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private NotesWidgetActivity injectNotesWidgetActivity2(NotesWidgetActivity instance) {
      NotesWidgetActivity_MembersInjector.injectNoteUseCase(instance, singletonCImpl.noteUseCase());
      NotesWidgetActivity_MembersInjector.injectSettingsRepository(instance, singletonCImpl.provideSettingsRepositoryProvider.get());
      return instance;
    }
  }

  private static final class ViewModelCImpl extends App_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<EditViewModel> editViewModelProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    GalleryObserver galleryObserver() {
      return new GalleryObserver(ApplicationModule_ProvideHandlerFactory.provideHandler(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));
    }

    SettingsUseCase settingsUseCase() {
      return new SettingsUseCase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideSettingsRepositoryProvider.get());
    }

    ImportExportUseCase importExportUseCase() {
      return new ImportExportUseCase(singletonCImpl.provideNoteRepositoryProvider.get(), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideImportExportRepositoryProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.editViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(3).put(EditViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (editViewModelProvider))).put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (homeViewModelProvider))).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (settingsViewModelProvider))).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.kin.easynotes.presentation.screens.edit.model.EditViewModel
          return (T) new EditViewModel(singletonCImpl.noteUseCase(), singletonCImpl.provideEncryptionHelperProvider.get());

          case 1: // com.kin.easynotes.presentation.screens.home.viewmodel.HomeViewModel
          return (T) new HomeViewModel(singletonCImpl.noteUseCase(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.kin.easynotes.presentation.screens.settings.model.SettingsViewModel
          return (T) new SettingsViewModel(viewModelCImpl.galleryObserver(), viewModelCImpl.settingsUseCase(), singletonCImpl.noteUseCase(), viewModelCImpl.importExportUseCase(), singletonCImpl.provideEncryptionHelperProvider.get(), singletonCImpl.provideKeystoreManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends App_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends App_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends App_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<NoteDatabaseProvider> provideNoteDatabaseProvider;

    Provider<NoteRepositoryImpl> provideNoteRepositoryProvider;

    Provider<CoroutineScope> provideCoroutineScopeProvider;

    Provider<KeystoreManager> provideKeystoreManagerProvider;

    Provider<EncryptionHelper> provideEncryptionHelperProvider;

    Provider<SettingsRepositoryImpl> provideSettingsRepositoryProvider;

    Provider<ExecutorCoroutineDispatcher> provideExecutorCoroutineDispatcherProvider;

    Provider<ImportExportRepository> provideImportExportRepositoryProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideNoteDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<NoteDatabaseProvider>(singletonCImpl, 1));
      this.provideNoteRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<NoteRepositoryImpl>(singletonCImpl, 0));
      this.provideCoroutineScopeProvider = DoubleCheck.provider(new SwitchingProvider<CoroutineScope>(singletonCImpl, 2));
      this.provideKeystoreManagerProvider = DoubleCheck.provider(new SwitchingProvider<KeystoreManager>(singletonCImpl, 4));
      this.provideEncryptionHelperProvider = DoubleCheck.provider(new SwitchingProvider<EncryptionHelper>(singletonCImpl, 3));
      this.provideSettingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepositoryImpl>(singletonCImpl, 5));
      this.provideExecutorCoroutineDispatcherProvider = DoubleCheck.provider(new SwitchingProvider<ExecutorCoroutineDispatcher>(singletonCImpl, 7));
      this.provideImportExportRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ImportExportRepository>(singletonCImpl, 6));
    }

    @Override
    public void injectApp(App app) {
    }

    @Override
    public NoteUseCase noteUseCase() {
      return new NoteUseCase(provideNoteRepositoryProvider.get(), provideCoroutineScopeProvider.get(), provideEncryptionHelperProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    @Override
    public SettingsRepositoryImpl settingsRepository() {
      return provideSettingsRepositoryProvider.get();
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.kin.easynotes.data.repository.NoteRepositoryImpl
          return (T) ApplicationModule_ProvideNoteRepositoryFactory.provideNoteRepository(singletonCImpl.provideNoteDatabaseProvider.get());

          case 1: // com.kin.easynotes.data.local.database.NoteDatabaseProvider
          return (T) ApplicationModule_ProvideNoteDatabaseProviderFactory.provideNoteDatabaseProvider(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 2: // kotlinx.coroutines.CoroutineScope
          return (T) ApplicationModule_ProvideCoroutineScopeFactory.provideCoroutineScope();

          case 3: // com.kin.easynotes.presentation.components.EncryptionHelper
          return (T) ApplicationModule_ProvideEncryptionHelperFactory.provideEncryptionHelper(singletonCImpl.provideKeystoreManagerProvider.get());

          case 4: // com.kin.easynotes.security.KeystoreManager
          return (T) ApplicationModule_ProvideKeystoreManagerFactory.provideKeystoreManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.kin.easynotes.data.repository.SettingsRepositoryImpl
          return (T) ApplicationModule_ProvideSettingsRepositoryFactory.provideSettingsRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.kin.easynotes.data.repository.ImportExportRepository
          return (T) ApplicationModule_ProvideImportExportRepositoryFactory.provideImportExportRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), ApplicationModule_ProvideMutexFactory.provideMutex(), singletonCImpl.provideCoroutineScopeProvider.get(), singletonCImpl.provideExecutorCoroutineDispatcherProvider.get());

          case 7: // kotlinx.coroutines.ExecutorCoroutineDispatcher
          return (T) ApplicationModule_ProvideExecutorCoroutineDispatcherFactory.provideExecutorCoroutineDispatcher();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
