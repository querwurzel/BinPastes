import {PasteView} from './api/model/PasteView';
import {PasteClone} from './components/CreatePaste/CreatePaste';

export interface IAppContext {
  pushPasteCloned: (data: PasteClone) => void
  popPasteCloned: () => PasteClone | undefined
  pushPasteCreated: (paste: PasteView) => void
  popPasteCreated: () => PasteView | undefined
  pushPasteDeleted: (paste: PasteView) => void
  onPasteCreated: (callback: (paste: PasteView) => void) => void
  onPasteDeleted: (callback: (paste: PasteView) => void) => void
}

class AppContextImpl implements IAppContext {

  private readonly creationEventHandlers: Array<(paste: PasteView) => void> = [];
  private readonly deletionEventHandlers: Array<(paste: PasteView) => void> = [];

  private pasteCloned?: PasteClone;
  private pasteCreated?: PasteView;

  pushPasteCloned(data: PasteClone) {
    this.pasteCloned = data;
  }

  popPasteCloned() : PasteClone | undefined {
    const holder = this.pasteCloned;
    delete this.pasteCloned;
    return holder;
  }

  pushPasteCreated(paste: PasteView) {
    this.pasteCreated = paste;
    this.creationEventHandlers.forEach(listener => listener(paste));
  }

  popPasteCreated() : PasteView | undefined {
    const holder = this.pasteCreated;
    delete this.pasteCreated;
    return holder;
  }

  onPasteCreated(callback: (paste: PasteView) => void) {
    this.creationEventHandlers.push(callback);
  }

  pushPasteDeleted(paste: PasteView) {
    this.deletionEventHandlers.forEach(listener => listener(paste));
  }

  onPasteDeleted(callback: (paste: PasteView) => void) {
    this.deletionEventHandlers.push(callback);
  }
}

const AppContext: IAppContext = new AppContextImpl();

export default AppContext;
