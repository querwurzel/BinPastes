
export type PasteListView = {
  id: string
  title?: string
  sizeInBytes: number
  isEncrypted: boolean
  dateCreated: string
  dateOfExpiry?: string
}

export type = PasteList {
  pastes: Array<PasteListView>
}
