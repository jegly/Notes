package com.kin.easynotes.presentation.screens.settings.model;

import com.kin.easynotes.data.repository.ImportExportRepository;
import com.kin.easynotes.domain.usecase.ImportExportUseCase;
import com.kin.easynotes.domain.usecase.NoteUseCase;
import com.kin.easynotes.domain.usecase.SettingsUseCase;
import com.kin.easynotes.presentation.components.EncryptionHelper;
import com.kin.easynotes.presentation.components.GalleryObserver;
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

  private final Provider<ImportExportRepository> backupProvider;

  private final Provider<SettingsUseCase> settingsUseCaseProvider;

  private final Provider<NoteUseCase> noteUseCaseProvider;

  private final Provider<ImportExportUseCase> importExportUseCaseProvider;

  private final Provider<EncryptionHelper> encryptionHelperProvider;

  private SettingsViewModel_Factory(Provider<GalleryObserver> galleryObserverProvider,
      Provider<ImportExportRepository> backupProvider,
      Provider<SettingsUseCase> settingsUseCaseProvider, Provider<NoteUseCase> noteUseCaseProvider,
      Provider<ImportExportUseCase> importExportUseCaseProvider,
      Provider<EncryptionHelper> encryptionHelperProvider) {
    this.galleryObserverProvider = galleryObserverProvider;
    this.backupProvider = backupProvider;
    this.settingsUseCaseProvider = settingsUseCaseProvider;
    this.noteUseCaseProvider = noteUseCaseProvider;
    this.importExportUseCaseProvider = importExportUseCaseProvider;
    this.encryptionHelperProvider = encryptionHelperProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(galleryObserverProvider.get(), backupProvider.get(), settingsUseCaseProvider.get(), noteUseCaseProvider.get(), importExportUseCaseProvider.get(), encryptionHelperProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<GalleryObserver> galleryObserverProvider,
      Provider<ImportExportRepository> backupProvider,
      Provider<SettingsUseCase> settingsUseCaseProvider, Provider<NoteUseCase> noteUseCaseProvider,
      Provider<ImportExportUseCase> importExportUseCaseProvider,
      Provider<EncryptionHelper> encryptionHelperProvider) {
    return new SettingsViewModel_Factory(galleryObserverProvider, backupProvider, settingsUseCaseProvider, noteUseCaseProvider, importExportUseCaseProvider, encryptionHelperProvider);
  }

  public static SettingsViewModel newInstance(GalleryObserver galleryObserver,
      ImportExportRepository backup, SettingsUseCase settingsUseCase, NoteUseCase noteUseCase,
      ImportExportUseCase importExportUseCase, EncryptionHelper encryptionHelper) {
    return new SettingsViewModel(galleryObserver, backup, settingsUseCase, noteUseCase, importExportUseCase, encryptionHelper);
  }
}
