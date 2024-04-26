
export interface PasteListView {
  id: string
  title?: string
  sizeInBytes: number
  isEncrypted: boolean
  dateCreated: string
  dateOfExpiry?: string
}

export interface PasteList {
  pastes: Array<PasteListView>
}
