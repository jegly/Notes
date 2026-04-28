package com.kin.easynotes.presentation.screens.settings.model;

import com.kin.easynotes.domain.usecase.ImportExportUseCase;
import com.kin.easynotes.domain.usecase.NoteUseCase;
import com.kin.easynotes.domain.usecase.SettingsUseCase;
import com.kin.easynotes.presentation.components.EncryptionHelper;
import com.kin.easynotes.presentation.components.GalleryObserver;
import com.kin.easynotes.security.KeystoreManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<GalleryObserver> galleryObserverProvider;

  private final Provider<SettingsUseCase> settingsUseCaseProvider;

  private final Provider<NoteUseCase> noteUseCaseProvider;

  private final Provider<ImportExportUseCase> importExportUseCaseProvider;

  private final Provider<EncryptionHelper> encryptionHelperProvider;

  private final Provider<KeystoreManager> keystoreManagerProvider;

  private SettingsViewModel_Factory(Provider<GalleryObserver> galleryObserverProvider,
      Provider<SettingsUseCase> settingsUseCaseProvider, Provider<NoteUseCase> noteUseCaseProvider,
      Provider<ImportExportUseCase> importExportUseCaseProvider,
      Provider<EncryptionHelper> encryptionHelperProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    this.galleryObserverProvider = galleryObserverProvider;
    this.settingsUseCaseProvider = settingsUseCaseProvider;
    this.noteUseCaseProvider = noteUseCaseProvider;
    this.importExportUseCaseProvider = importExportUseCaseProvider;
    this.encryptionHelperProvider = encryptionHelperProvider;
    this.keystoreManagerProvider = keystoreManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(galleryObserverProvider.get(), settingsUseCaseProvider.get(), noteUseCaseProvider.get(), importExportUseCaseProvider.get(), encryptionHelperProvider.get(), keystoreManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<GalleryObserver> galleryObserverProvider,
      Provider<SettingsUseCase> settingsUseCaseProvider, Provider<NoteUseCase> noteUseCaseProvider,
      Provider<ImportExportUseCase> importExportUseCaseProvider,
      Provider<EncryptionHelper> encryptionHelperProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    return new SettingsViewModel_Factory(galleryObserverProvider, settingsUseCaseProvider, noteUseCaseProvider, importExportUseCaseProvider, encryptionHelperProvider, keystoreManagerProvider);
  }

  public static SettingsViewModel newInstance(GalleryObserver galleryObserver,
      SettingsUseCase settingsUseCase, NoteUseCase noteUseCase,
      ImportExportUseCase importExportUseCase, EncryptionHelper encryptionHelper,
      KeystoreManager keystoreManager) {
    return new SettingsViewModel(galleryObserver, settingsUseCase, noteUseCase, importExportUseCase, encryptionHelper, keystoreManager);
  }
}
