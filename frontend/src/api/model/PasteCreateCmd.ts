
export interface PasteCreateCmd {
  title?: string
  content: string
  isEncrypted?: boolean
  expiry?: 'ONE_HOUR' | 'ONE_DAY' | 'ONE_WEEK' | 'ONE_MONTH' | 'ONE_YEAR' | 'NEVER'
  exposure?: 'PUBLIC' | 'UNLISTED' | 'ONCE'
}
