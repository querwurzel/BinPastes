import {PasteView} from './api/model/PasteView';
import {PasteClone} from './components/CreatePaste/CreatePaste';

interface IAppContext {
  pushPasteCloned: (data: PasteClone) => void
  popPasteCloned: () => PasteClone | undefined
  pushPasteCreated: (paste: PasteView) => void
  popPasteCreated: () => PasteView | undefined
  onPasteCreated: (callback: (paste: PasteView) => void) => void
  onPasteDeleted: (callback: (paste: PasteView) => void) => void
}

class AppContextImpl implements IAppContext {

  private readonly creationEventHandler: Array<(paste: PasteView) => void> = [];
  private readonly deletionEventHandler: Array<(paste: PasteView) => void> = [];

  private pasteCloned: PasteClone = null;
  private pasteCreated: PasteView = null;

  pushPasteCloned(data: PasteClone) {
    this.pasteCloned = data;
  }

  popPasteCloned(): PasteClone | null {
    const holder = this.pasteCloned;
    this.pasteCloned = null;
    return holder;
  }

  pushPasteCreated(paste: PasteView) {
    this.pasteCreated = paste;
    this.creationEventHandler.forEach(listener => listener(paste));
  }

  popPasteCreated(): PasteView | null {
    const holder = this.pasteCreated;
    this.pasteCreated = null;
    return holder;
  }

  pushPasteDeleted(paste: PasteView) {
    this.deletionEventHandler.forEach(listener => listener(paste));
  }

  onPasteCreated(callback: (paste: PasteView) => void) {
    this.creationEventHandler.push(callback);
  }

  onPasteDeleted(callback: (paste: PasteView) => void) {
    this.deletionEventHandler.push(callback);
  }
}

const AppContext: IAppContext = new AppContextImpl();

export default AppContext;
