import {PasteView} from './api/model/PasteView';

export type PasteClone = {
  title?: string
  content: string
}

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
    const ref = this.pasteCloned;
    delete this.pasteCloned;
    return ref;
  }

  pushPasteCreated(paste: PasteView) {
    this.pasteCreated = paste;
    this.creationEventHandlers.forEach(listener => listener(paste));
  }

  popPasteCreated() : PasteView | undefined {
    const ref = this.pasteCreated;
    delete this.pasteCreated;
    return ref;
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
