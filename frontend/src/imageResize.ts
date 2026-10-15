// Phone photos are often 5-10 MB. Before uploading, the photo is shrunk to at most maxSide pixels on its longest side
// and saved as JPEG: the page loads fast and the upload is always small, whatever the original was.
export async function shrinkImage(file: File, maxSide = 1600): Promise<Blob> {
  const bitmap = await createImageBitmap(file).catch(() => {
    throw new Error('Kunne ikke lese bildet. Velg en JPG-, PNG- eller WebP-fil.')
  })
  const scale = Math.min(1, maxSide / Math.max(bitmap.width, bitmap.height))
  const canvas = document.createElement('canvas')
  canvas.width = Math.round(bitmap.width * scale)
  canvas.height = Math.round(bitmap.height * scale)
  canvas.getContext('2d')!.drawImage(bitmap, 0, 0, canvas.width, canvas.height)
  bitmap.close()

  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error('Kunne ikke behandle bildet.'))),
      'image/jpeg',
      0.85,
    )
  })
}
