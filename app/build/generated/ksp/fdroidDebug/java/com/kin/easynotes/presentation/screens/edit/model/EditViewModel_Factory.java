package com.kin.easynotes.presentation.screens.edit.model;

import com.kin.easynotes.domain.usecase.NoteUseCase;
import com.kin.easynotes.presentation.components.EncryptionHelper;
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
public final class EditViewModel_Factory implements Factory<EditViewModel> {
  private final Provider<NoteUseCase> noteUseCaseProvider;

  private final Provider<EncryptionHelper> encryptionProvider;

  private EditViewModel_Factory(Provider<NoteUseCase> noteUseCaseProvider,
      Provider<EncryptionHelper> encryptionProvider) {
    this.noteUseCaseProvider = noteUseCaseProvider;
    this.encryptionProvider = encryptionProvider;
  }

  @Override
  public EditViewModel get() {
    return newInstance(noteUseCaseProvider.get(), encryptionProvider.get());
  }

  public static EditViewModel_Factory create(Provider<NoteUseCase> noteUseCaseProvider,
      Provider<EncryptionHelper> encryptionProvider) {
    return new EditViewModel_Factory(noteUseCaseProvider, encryptionProvider);
  }

  public static EditViewModel newInstance(NoteUseCase noteUseCase, EncryptionHelper encryption) {
    return new EditViewModel(noteUseCase, encryption);
  }
}
