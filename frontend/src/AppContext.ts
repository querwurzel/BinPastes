import {PasteView} from './api/model/PasteView';
import {CloneModel} from './components/CreatePaste/CreatePaste';

interface IAppContext {
  pushClonedPaste: (data: CloneModel) => void
  popClonedPaste: () => CloneModel | undefined
  pushCreatedPaste: (paste: PasteView) => void
  popCreatedPaste: () => PasteView | undefined
  onPasteCreated: (callback: (paste: PasteView) => void) => void
}

class AppContextImpl implements IAppContext {

  private readonly listeners: Array<(paste: PasteView) => void> = [];

  private clone: CloneModel = null;

  private pasteCreated: PasteView = null;

  pushClonedPaste(data: CloneModel) {
    this.clone = data;
  }

  popClonedPaste(): CloneModel | undefined {
    const holder = this.clone;
    this.clone = null;
    return holder;
  }

  pushCreatedPaste(paste: PasteView) {
    this.pasteCreated = paste;
    this.listeners.forEach(listener => listener(paste));
  }

  onPasteCreated(callback: (paste: PasteView) => void) {
    this.listeners.push(callback);
  }

  popCreatedPaste(): PasteView | undefined {
    const holder = this.pasteCreated;
    this.pasteCreated = null;
    return holder;
  }
}

const AppContext: IAppContext = new AppContextImpl();

export default AppContext;
