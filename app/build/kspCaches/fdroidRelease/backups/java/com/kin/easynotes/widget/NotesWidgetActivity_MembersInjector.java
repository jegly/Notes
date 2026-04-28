package com.kin.easynotes.widget;

import com.kin.easynotes.data.repository.SettingsRepositoryImpl;
import com.kin.easynotes.domain.usecase.NoteUseCase;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class NotesWidgetActivity_MembersInjector implements MembersInjector<NotesWidgetActivity> {
  private final Provider<NoteUseCase> noteUseCaseProvider;

  private final Provider<SettingsRepositoryImpl> settingsRepositoryProvider;

  private NotesWidgetActivity_MembersInjector(Provider<NoteUseCase> noteUseCaseProvider,
      Provider<SettingsRepositoryImpl> settingsRepositoryProvider) {
    this.noteUseCaseProvider = noteUseCaseProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  public static MembersInjector<NotesWidgetActivity> create(
      Provider<NoteUseCase> noteUseCaseProvider,
      Provider<SettingsRepositoryImpl> settingsRepositoryProvider) {
    return new NotesWidgetActivity_MembersInjector(noteUseCaseProvider, settingsRepositoryProvider);
  }

  @Override
  public void injectMembers(NotesWidgetActivity instance) {
    injectNoteUseCase(instance, noteUseCaseProvider.get());
    injectSettingsRepository(instance, settingsRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.kin.easynotes.widget.NotesWidgetActivity.noteUseCase")
  public static void injectNoteUseCase(NotesWidgetActivity instance, NoteUseCase noteUseCase) {
    instance.noteUseCase = noteUseCase;
  }

  @InjectedFieldSignature("com.kin.easynotes.widget.NotesWidgetActivity.settingsRepository")
  public static void injectSettingsRepository(NotesWidgetActivity instance,
      SettingsRepositoryImpl settingsRepository) {
    instance.settingsRepository = settingsRepository;
  }
}
